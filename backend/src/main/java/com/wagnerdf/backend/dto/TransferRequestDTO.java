package com.wagnerdf.backend.dto;

import com.wagnerdf.backend.enums.TransferFeeRule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequestDTO(

        @NotNull(message = "É necessário o ID da conta de origem.")
        Long originAccountId,

        @NotNull(message = "É necessário o ID da conta de destino.")
        Long destinationAccountId,

        @NotNull(message = "É necessário informar o valor.")
        @Positive(message = "O valor deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "É necessário informar a regra de taxa.")
        TransferFeeRule feeRule
) {}
