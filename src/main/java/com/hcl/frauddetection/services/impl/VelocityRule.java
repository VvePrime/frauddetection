package com.hcl.frauddetection.services.impl;

import com.hcl.frauddetection.entities.Transaction;
import com.hcl.frauddetection.records.RuleScoreDTO;
import com.hcl.frauddetection.records.TransactionRequest;
import com.hcl.frauddetection.services.RiskRule;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class VelocityRule implements RiskRule {

    @Override
    @Async("ruleExecutor")
    public CompletableFuture<RuleScoreDTO> evaluate(TransactionRequest req, List<Transaction> history) {
        long count = history.stream()
                .filter(t -> t.getTimestamp().isAfter(Instant.now().minus(5, ChronoUnit.MINUTES)))
                .count();

        int score = (count >= 6) ? 30 : (count >= 4) ? 15 : 0;
        return CompletableFuture.completedFuture(new RuleScoreDTO("VELOCITY", score, count + " txns in 5 mins"));
    }
}
