package com.wagnerdf.backend.enums;

public enum TransactionCategory {
	
    DEPOSIT,		// Deposito feito pelo usuário
    WITHDRAW,		// Quando o usuário retira saldo da conta
    TRANSFER,		// Transferencia de saldo feita pelo usuário o sistema
    FEE,   			// Quando for uma tarifa do sistema
    BET,            // Quando usuário faz aposta
    PRIZE,          // Quando usuário recebe prêmio
    PLATFORM_FEE    // Taxa retida pela plataforma

}
