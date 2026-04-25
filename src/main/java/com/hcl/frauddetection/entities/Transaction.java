package com.hcl.frauddetection.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    private String transactionId;

    @Column(name = "user_id")
    private Long userId;

    private Double amount;

    private Double latitude;
    private Double longitude;

    private Instant timestamp;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "total_score")
    private Integer totalScore;
}
