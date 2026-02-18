package com.wagnerdf.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementResponseDTO {
    private Long accountId;
    private String accountNumber;
    private String accountHolder;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal initialBalance;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal finalBalance;
    private List<StatementTransactionDTO> transactions;
}
