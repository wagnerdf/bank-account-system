package com.wagnerdf.backend;

import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.dto.StatementTransactionDTO;

import java.time.format.DateTimeFormatter;

public class StatementPrinter {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";         // DEBIT
    private static final String GREEN = "\u001B[32m";       // CREDIT / PRIZE
    private static final String CYAN = "\u001B[36m";        // Cabeçalho
    private static final String YELLOW = "\u001B[33m";      // Totais
    private static final String MAGENTA = "\u001B[35m";     // BET
    private static final String BLUE = "\u001B[34m";        // PLATFORM_FEE

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void printFancyStatement(StatementResponseDTO statement) {
        System.out.println(CYAN + "================== EXTRATO BANCÁRIO ==================" + RESET);
        System.out.println("Conta: " + statement.getAccountNumber() + "   Titular: " + statement.getAccountHolder());
        System.out.println("Período: " + statement.getStartDate() + " a " + statement.getEndDate());
        System.out.printf("Saldo Inicial: %.2f%n", statement.getInitialBalance());
        System.out.println("------------------------------------------------------");
        System.out.printf("%-5s %-16s %-15s %-12s %-8s %-12s %-20s%n",
                "ID", "Data/Hora", "Tipo", "Valor", "Taxa", "Saldo", "Descrição");
        System.out.println("------------------------------------------------------");

        for (StatementTransactionDTO t : statement.getTransactions()) {
            String typeLabel;
            String color;

            switch (t.getType()) {
                case CREDIT:
                    typeLabel = "CREDIT";
                    color = GREEN;
                    break;
                case PRIZE:
                    typeLabel = "PRIZE";
                    color = GREEN;
                    break;
                case DEBIT:
                    typeLabel = "DEBIT";
                    color = RED;
                    break;
                case BET:
                    typeLabel = "BET";
                    color = MAGENTA;
                    break;
                case PLATFORM_FEE:
                    typeLabel = "FEE";
                    color = BLUE;
                    break;
                default:
                    typeLabel = t.getType().name();
                    color = RESET;
            }

            System.out.printf(color + "%-5d %-16s %-15s %-12.2f %-8.2f %-12.2f %-20s" + RESET + "%n",
                    t.getId(),
                    t.getCreatedAt().format(dtf),
                    typeLabel,
                    t.getAmount(),
                    t.getAppliedTax(),
                    t.getBalanceAfter(),
                    t.getDescription()
            );
        }

        System.out.println("------------------------------------------------------");
        System.out.printf(YELLOW + "Total Créditos: %-10.2f%n" + RESET, statement.getTotalCredits());
        System.out.printf(YELLOW + "Total Débitos:  %-10.2f%n" + RESET, statement.getTotalDebits());
        System.out.printf(YELLOW + "Saldo Final:    %-10.2f%n" + RESET, statement.getFinalBalance());
        System.out.println(CYAN + "======================================================" + RESET);
    }
}