package com.wagnerdf.backend.dto;

import java.math.BigDecimal;

public record TransferResponseDTO(
        Long originAccountId,
        BigDecimal originNewBalance,
        Long destinationAccountId,
        BigDecimal destinationNewBalance,
        BigDecimal amount,
        BigDecimal appliedFee
) {}
