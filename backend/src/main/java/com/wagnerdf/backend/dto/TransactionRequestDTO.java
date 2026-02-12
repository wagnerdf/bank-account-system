package com.wagnerdf.backend.dto;

import com.wagnerdf.backend.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequestDTO(

        @NotNull(message = "É necessário o ID da conta.")
        Long accountId,

        @NotNull(message = "É necessário indicar o tipo de transação.")
        TransactionType type,

        @NotNull(message = "É necessário informar o valor.")
        @Positive(message = "O valor deve ser maior que zero.")
        BigDecimal amount
) {}
