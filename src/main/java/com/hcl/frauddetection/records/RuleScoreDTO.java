package com.hcl.frauddetection.records;

public record RuleScoreDTO(String rule, Integer score, String reason) {
}
