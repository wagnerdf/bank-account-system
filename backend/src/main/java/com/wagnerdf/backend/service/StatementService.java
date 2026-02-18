package com.wagnerdf.backend.service;

import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.dto.StatementTransactionDTO;
import com.wagnerdf.backend.enums.TransactionType;
import com.wagnerdf.backend.exception.BusinessException;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.model.Transaction;
import com.wagnerdf.backend.repository.BankAccountRepository;
import com.wagnerdf.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatementService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    // =====================================================
    // 1️⃣ GERAR EXTRATO (STATEMENT)
    // =====================================================
    public StatementResponseDTO getStatement(Long accountId, LocalDate startDate, LocalDate endDate) {

        // 1️⃣ Buscar conta com usuário
        BankAccount account = bankAccountRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new BusinessException("Conta não encontrada", HttpStatus.NOT_FOUND));

        // 2️⃣ Saldo inicial antes do período
        BigDecimal initialBalance = transactionRepository.calculateBalanceBeforeDate(
                accountId,
                startDate.atStartOfDay()
        );

        // 3️⃣ Buscar transações do período
        List<Transaction> transactions = transactionRepository
                .findByAccountIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                        accountId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        // 4️⃣ Inicializar variáveis de totais e saldo progressivo
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal runningBalance = initialBalance;

        List<StatementTransactionDTO> transactionDTOs = new ArrayList<>();

        // 5️⃣ Loop pelas transações para criar DTOs com saldo após
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.CREDIT) {
                totalCredits = totalCredits.add(t.getAmount());
                runningBalance = runningBalance.add(t.getAmount());
            } else { // DEBIT
                BigDecimal debitWithTax = t.getAmount().add(
                        t.getAppliedTax() != null ? t.getAppliedTax() : BigDecimal.ZERO
                );
                totalDebits = totalDebits.add(debitWithTax);
                runningBalance = runningBalance.subtract(debitWithTax);
            }

            // 5️⃣1️⃣ Montar DTO de transação
            StatementTransactionDTO dto = StatementTransactionDTO.builder()
                    .id(t.getId())
                    .createdAt(t.getCreatedAt())
                    .description(t.getDescription() != null ? t.getDescription() : "")
                    .type(t.getType())
                    .amount(t.getAmount())
                    .appliedTax(t.getAppliedTax())
                    .balanceAfter(runningBalance)
                    .build();

            transactionDTOs.add(dto);
        }

        // 6️⃣ Montar DTO final do extrato
        return StatementResponseDTO.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getUser().getFullName())
                .startDate(startDate)
                .endDate(endDate)
                .initialBalance(initialBalance)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .finalBalance(runningBalance)
                .transactions(transactionDTOs)
                .build();
    }
}
