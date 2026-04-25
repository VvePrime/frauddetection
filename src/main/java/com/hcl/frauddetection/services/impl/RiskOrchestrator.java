package com.hcl.frauddetection.services.impl;

import com.hcl.frauddetection.entities.Transaction;
import com.hcl.frauddetection.records.RiskAssessmentResponse;
import com.hcl.frauddetection.records.RuleScoreDTO;
import com.hcl.frauddetection.records.TransactionRequest;
import com.hcl.frauddetection.repositories.TransactionRepository;
import com.hcl.frauddetection.services.RiskRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RiskOrchestrator {
    private final List<RiskRule> rules;
    private final TransactionRepository repository;

    public RiskOrchestrator(List<RiskRule> rules, TransactionRepository repository) {
        this.rules = rules;
        this.repository = repository;
    }

    public RiskAssessmentResponse assess(TransactionRequest request) {
        long start = System.currentTimeMillis();
        List<Transaction> history = repository.findTop50ByUserIdOrderByTimestampDesc(request.userId());

        // Trigger all rules in parallel
        List<CompletableFuture<RuleScoreDTO>> futures = rules.stream()
                .map(rule -> rule.evaluate(request, history))
                .toList();

        // Aggregate results
        List<RuleScoreDTO> scores = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        int totalScore = scores.stream().mapToInt(RuleScoreDTO::score).sum();

        String decision = mapScoreToDecision(totalScore);

        // 5. Save record if NOT BLOCKED
        if (!"BLOCK".equalsIgnoreCase(decision)) {
            saveToDatabase(request, totalScore, decision);
        }

        //Logic for decisions other than APPROVE.
        //send events to other services - for hold, verify.

        return new RiskAssessmentResponse(
                request.transactionId(),
                decision,
                totalScore,
                scores,
                System.currentTimeMillis() - start
        );
    }

    private String mapScoreToDecision(int score) {
        if (score > 80) return "BLOCK";
        if (score > 60) return "HOLD";
        if (score > 40) return "VERIFY";
        return "APPROVE";
    }

    private void saveToDatabase(TransactionRequest req, int score, String decision) {
        Transaction txn = new Transaction();
        txn.setTransactionId(req.transactionId());
        txn.setUserId(req.userId());
        txn.setAmount(req.amount());
        txn.setLatitude(req.latitude());
        txn.setLongitude(req.longitude());
        txn.setTimestamp(java.time.Instant.now());
        txn.setTotalScore(score);
        txn.setRiskLevel(decision); // Storing the decision/risk status

        repository.save(txn);
    }
}
