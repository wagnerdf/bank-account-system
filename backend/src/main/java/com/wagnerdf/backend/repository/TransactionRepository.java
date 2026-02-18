package com.wagnerdf.backend.repository;

import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountId(Long accountId);
    
    @Query("""
            SELECT COALESCE(SUM(
                CASE 
                    WHEN t.type = 'CREDIT' THEN t.amount
                    ELSE -(t.amount + t.appliedTax)
                END
            ), 0)
            FROM Transaction t
            WHERE t.account.id = :accountId
            AND t.createdAt < :date
        """)
        BigDecimal calculateBalanceBeforeDate(
            @Param("accountId") Long accountId,
            @Param("date") LocalDateTime date
        );

        List<Transaction> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            Long accountId,
            LocalDateTime start,
            LocalDateTime end
        );
}
