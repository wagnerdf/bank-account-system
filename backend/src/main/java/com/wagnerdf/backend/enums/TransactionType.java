package com.wagnerdf.backend.enums;

public enum TransactionType {
    DEBIT,
    CREDIT,
    TRANSFER,
    FEE,   			// Quando for uma tarifa do sistema
    BET,            // Quando usuário faz aposta
    PRIZE,          // Quando usuário recebe prêmio
    PLATFORM_FEE    // Taxa retida pela plataforma
}
