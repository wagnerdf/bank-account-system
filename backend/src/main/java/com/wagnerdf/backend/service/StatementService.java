package com.wagnerdf.backend.service;

import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.dto.StatementTransactionDTO;
import com.wagnerdf.backend.enums.TransactionCategory;
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
    public StatementResponseDTO getStatement(Long accountId,
                                             LocalDate startDate,
                                             LocalDate endDate) {

        // 1️⃣ Buscar conta com usuário
        BankAccount account = bankAccountRepository.findByIdWithUser(accountId)
                .orElseThrow(() ->
                        new BusinessException("Conta não encontrada", HttpStatus.NOT_FOUND));

        // 2️⃣ Saldo antes do período
        BigDecimal initialBalance = transactionRepository
                .calculateBalanceBeforeDate(accountId, startDate.atStartOfDay());

        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }

        // 3️⃣ Buscar transações ordenadas
        List<Transaction> transactions =
                transactionRepository.findByAccountAndCreatedAtBetween(
                        accountId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal runningBalance = initialBalance;

        List<StatementTransactionDTO> transactionDTOs = new ArrayList<>();

        // 4️⃣ Processar transações
        for (Transaction t : transactions) {

            BigDecimal amount = t.getAmount() != null
                    ? t.getAmount()
                    : BigDecimal.ZERO;

            BigDecimal tax = t.getAppliedTax() != null
                    ? t.getAppliedTax()
                    : BigDecimal.ZERO;
            
            TransactionCategory category = t.getCategory();

            switch (category) {

                // ================= CREDITOS =================
                case DEPOSIT:
                case PRIZE:
                    totalCredits = totalCredits.add(amount);
                    runningBalance = runningBalance.add(amount);
                    break;

                // ================= DEBITOS =================
                case WITHDRAW:
                case BET:
                case PLATFORM_FEE:
                    BigDecimal debitWithTax = amount.add(tax);
                    totalDebits = totalDebits.add(debitWithTax);
                    runningBalance = runningBalance.subtract(debitWithTax);
                    break;

                default:
                    throw new BusinessException(
                            "Tipo de transação não suportado: " + t.getType(),
                            HttpStatus.BAD_REQUEST
                    );
            }

            // Montar DTO
            StatementTransactionDTO dto = StatementTransactionDTO.builder()
                    .id(t.getId())
                    .createdAt(t.getCreatedAt())
                    .description(t.getDescription() != null ? t.getDescription() : "")
                    .type(t.getType())
                    .amount(amount)
                    .appliedTax(tax)
                    .balanceAfter(runningBalance)
                    .build();

            transactionDTOs.add(dto);
        }

        // 5️⃣ Retornar DTO final
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