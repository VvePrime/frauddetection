package com.hcl.frauddetection.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    // Relationship to transactions (optional, for O/RM navigation)
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}
