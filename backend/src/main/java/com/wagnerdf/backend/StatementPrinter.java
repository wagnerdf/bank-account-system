package com.wagnerdf.backend;

import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.dto.StatementTransactionDTO;
import com.wagnerdf.backend.enums.TransactionType;

public class StatementPrinter {

    // Códigos ANSI para cores
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";

    public static void printFancyStatement(StatementResponseDTO statement) {
        System.out.println(CYAN + "==============================================================" + RESET);
        System.out.println(CYAN + "                       EXTRATO BANCÁRIO" + RESET);
        System.out.println(CYAN + "==============================================================" + RESET);
        System.out.println("Conta: " + statement.getAccountNumber() + "   Titular: " + statement.getAccountHolder());
        System.out.println("Período: " + statement.getStartDate() + " a " + statement.getEndDate());
        System.out.println("Saldo Inicial: " + String.format("%.2f", statement.getInitialBalance()));
        System.out.println("--------------------------------------------------------------");

        System.out.printf("%-5s %-20s %-10s %-10s %-10s %-10s%n",
                "ID", "Data/Hora", "Tipo", "Valor", "Taxa", "Saldo");
        System.out.println("--------------------------------------------------------------");

        for (StatementTransactionDTO t : statement.getTransactions()) {
            String typeSymbol;
            String color;
            if (t.getType() == TransactionType.CREDIT) {
                typeSymbol = "CRÉDITO";
                color = GREEN; // Créditos em verde
            } else {
                typeSymbol = "DÉBITO";
                color = RED;   // Débitos em vermelho
            }

            System.out.printf(color + "%-5d %-20s %-10s %-10.2f %-10.2f %-10.2f" + RESET + "%n",
                    t.getId(),
                    t.getCreatedAt(),
                    typeSymbol,
                    t.getAmount(),
                    t.getAppliedTax(),
                    t.getBalanceAfter());
        }

        System.out.println("--------------------------------------------------------------");
        System.out.printf(YELLOW + "Total Créditos: %-10.2f%n" + RESET, statement.getTotalCredits());
        System.out.printf(YELLOW + "Total Débitos:  %-10.2f%n" + RESET, statement.getTotalDebits());
        System.out.printf(YELLOW + "Saldo Final:    %-10.2f%n" + RESET, statement.getFinalBalance());
        System.out.println(CYAN + "==============================================================" + RESET);
    }
}
