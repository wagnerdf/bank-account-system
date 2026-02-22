package com.wagnerdf.backend.dto;

import java.math.BigDecimal;

public record CreditRequestDTO(
        Long toAccountId,
        BigDecimal amount,
        String description
) {}
