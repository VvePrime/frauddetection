package com.hcl.frauddetection.records;

import java.util.List;

public record RiskAssessmentResponse(String transactionId,
                                     String decision,
                                     Integer totalScore,
                                     List<RuleScoreDTO> ruleScores,
                                     Long processingTimeMs) {
}
