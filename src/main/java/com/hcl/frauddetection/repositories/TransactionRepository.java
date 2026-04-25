package com.hcl.frauddetection.repositories;

import com.hcl.frauddetection.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // MySQL optimized: find top 50 records for a specific user
    List<Transaction> findTop50ByUserIdOrderByTimestampDesc(Long userId);

    @Query(value = "SELECT AVG(amount) FROM transactions WHERE user_id = :userId", nativeQuery = true)
    Double getAverageTransactionAmount(@Param("userId") Long userId);
}