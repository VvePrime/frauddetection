package com.hcl.frauddetection.services;

import com.hcl.frauddetection.entities.Transaction;
import com.hcl.frauddetection.records.RuleScoreDTO;
import com.hcl.frauddetection.records.TransactionRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RiskRule {
    CompletableFuture<RuleScoreDTO> evaluate(TransactionRequest request, List<Transaction> history);

}
