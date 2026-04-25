package com.hcl.frauddetection.services.impl;

import com.hcl.frauddetection.entities.Transaction;
import com.hcl.frauddetection.records.RuleScoreDTO;
import com.hcl.frauddetection.records.TransactionRequest;
import com.hcl.frauddetection.services.RiskRule;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AmountSpikeRule implements RiskRule {
    @Override
    @Async("ruleExecutor")
    public CompletableFuture<RuleScoreDTO> evaluate(TransactionRequest req, List<Transaction> history) {
        double avg = history.stream().mapToDouble(Transaction::getAmount).average().orElse(req.amount());
        double ratio = req.amount() / avg;

        int score = 0;
        if (ratio >= 5) score = 30;
        else if (ratio >= 3) score = 20;
        else if (ratio >= 2) score = 10;

        return CompletableFuture.completedFuture(new RuleScoreDTO("AMOUNT_SPIKE", score, String.format("%.1fx average", ratio)));
    }
}
