package com.hcl.frauddetection.services.impl;

import com.hcl.frauddetection.entities.Transaction;
import com.hcl.frauddetection.records.RuleScoreDTO;
import com.hcl.frauddetection.records.TransactionRequest;
import com.hcl.frauddetection.services.RiskRule;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GeoAnomalyRule implements RiskRule {
    private static final double CITY_RADIUS_KM = 30.0;
    private static final double COUNTRY_RADIUS_KM = 1000.0;

    @Override
    @Async("ruleExecutor")
    public CompletableFuture<RuleScoreDTO> evaluate(TransactionRequest req, List<Transaction> history) {
        if (history.isEmpty()) {
            return CompletableFuture.completedFuture(new RuleScoreDTO("GEO_ANOMALY", 0, "Initial transaction"));
        }

        Transaction last = history.getFirst();
        double distanceKm = calculateDistance(req.latitude(), req.longitude(),
                last.getLatitude(), last.getLongitude());

        long timeDiffSeconds = Math.abs(Duration.between(last.getTimestamp(), Instant.now()).getSeconds());
        double hoursBetween = timeDiffSeconds / 3600.0;
        double speedKmh = distanceKm / (hoursBetween + 0.0001); // Prevent div by zero

        int score;
        String reason;

        // 1. Physically Impossible (500 miles/800km in 10 mins is ~4800 km/h)
        if (speedKmh > 900) {
            score = 40;
            reason = String.format("Impossible speed: %.2f km/h", speedKmh);
        }
        // 2. Different Countries (Using average country radius ~1000km)
        else if (distanceKm > COUNTRY_RADIUS_KM) {
            score = 30; // High range for unlikely timing
            reason = "Potential international travel anomaly";
        }
        // 3. Different Cities (Using average city radius ~30km)
        else if (distanceKm > CITY_RADIUS_KM) {
            score = 10;
            reason = "Rapid travel between cities";
        }
        // 4. Same City / Reasonable
        else {
            score = 0;
            reason = "Within acceptable geographic range";
        }

        return CompletableFuture.completedFuture(new RuleScoreDTO("GEO_ANOMALY", score, reason));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine Implementation
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
