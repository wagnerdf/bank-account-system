package com.wagnerdf.backend.dto;

import java.math.BigDecimal;

public record DebitRequestDTO(
    Long fromAccountId,
    BigDecimal amount,
    BigDecimal fee,          // opcional
    String description       // opcional
) {}
