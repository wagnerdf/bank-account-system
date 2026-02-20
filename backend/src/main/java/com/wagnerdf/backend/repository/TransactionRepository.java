package com.wagnerdf.backend.repository;

import com.wagnerdf.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // =====================================================
    // 1️⃣ Todas as transações de uma conta (entrada e saída)
    // =====================================================
    @Query("""
        SELECT t 
        FROM Transaction t 
        WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId
        ORDER BY t.createdAt ASC
    """)
    List<Transaction> findByAccount(@Param("accountId") Long accountId);

    // =====================================================
    // 2️⃣ Extrato entre datas (entrada e saída)
    // =====================================================
    @Query("""
        SELECT t 
        FROM Transaction t 
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
          AND t.createdAt BETWEEN :start AND :end
        ORDER BY t.createdAt ASC
    """)
    List<Transaction> findByAccountAndCreatedAtBetween(
            @Param("accountId") Long accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // =====================================================
    // 3️⃣ Saldo antes de uma data
    // =====================================================
    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN t.toAccount.id = :accountId THEN t.amount
                WHEN t.fromAccount.id = :accountId THEN -(t.amount + t.appliedTax)
                ELSE 0
            END
        ), 0)
        FROM Transaction t
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
          AND t.createdAt < :date
          AND t.status = 'COMPLETED'
    """)
    BigDecimal calculateBalanceBeforeDate(
            @Param("accountId") Long accountId,
            @Param("date") LocalDateTime date
    );
}