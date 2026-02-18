package com.wagnerdf.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wagnerdf.backend.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementTransactionDTO {

    private Long id;

    private LocalDateTime createdAt;

    private String description;

    private TransactionType type;

    private BigDecimal amount;

    private BigDecimal appliedTax;

    private BigDecimal balanceAfter;
}
