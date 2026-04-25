package com.hcl.frauddetection.controllers;

import com.hcl.frauddetection.records.RiskAssessmentResponse;
import com.hcl.frauddetection.records.TransactionRequest;
import com.hcl.frauddetection.services.impl.RiskOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final RiskOrchestrator riskOrchestrator;

    public TransactionController(RiskOrchestrator riskOrchestrator) {
        this.riskOrchestrator = riskOrchestrator;
    }

    /**
     * Single entry point to assess and process a transaction.
     *
     * @param request The incoming transaction data (amount, location, etc.)
     * @return RiskAssessmentResponse including decision and rule breakdown
     */
    @PostMapping("/process")
    public ResponseEntity<RiskAssessmentResponse> processTransaction(@RequestBody TransactionRequest request) {

        // 1. Trigger the orchestration (rules run in parallel here)
        RiskAssessmentResponse response = riskOrchestrator.assess(request);

        // 2. Determine HTTP Status based on the decision
        // We use 403 Forbidden for BLOCK to signal a security/risk refusal
        if ("BLOCK".equalsIgnoreCase(response.decision())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // 3. For all other states (APPROVE, VERIFY, HOLD), return 200 OK
        // The client will check the 'decision' field to know if they need to prompt for OTP
        return ResponseEntity.ok(response);
    }
}
