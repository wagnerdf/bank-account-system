package com.wagnerdf.backend.service;

import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.dto.TransferResponseDTO;
import com.wagnerdf.backend.enums.TransactionStatus;
import com.wagnerdf.backend.enums.TransactionType;
import com.wagnerdf.backend.exception.BusinessException;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.model.Transaction;
import com.wagnerdf.backend.repository.BankAccountRepository;
import com.wagnerdf.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    // =====================================================
    // 1️⃣ CRÉDITO
    // =====================================================
    @Transactional
    public TransactionResponseDTO credit(
            BankAccount toAccount,
            BigDecimal amount,
            String description
    ) {
        // 1️⃣ Validar valor
        validateAmount(amount);

        // 2️⃣ Atualizar saldo
        toAccount.setBalance(toAccount.getBalance().add(amount));
        bankAccountRepository.save(toAccount);

        // 3️⃣ Criar transação
        Transaction transaction = Transaction.builder()
                .toAccount(toAccount)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .appliedTax(BigDecimal.ZERO)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        transactionRepository.save(transaction);

        return buildResponse(transaction, toAccount.getId());
    }

    // =====================================================
    // 2️⃣ DÉBITO
    // =====================================================
    @Transactional
    public TransactionResponseDTO debit(
            BankAccount fromAccount,
            BigDecimal amount,
            BigDecimal fee,
            String description
    ) {
        validateAmount(amount);

        BigDecimal totalDebit = amount.add(fee);
        if (fromAccount.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessException("Saldo insuficiente", HttpStatus.CONFLICT);
        }

        // Atualiza saldo
        fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));
        bankAccountRepository.save(fromAccount);

        // Cria transação
        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .appliedTax(fee)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        transactionRepository.save(transaction);

        return buildResponse(transaction, fromAccount.getId());
    }

    // =====================================================
    // 3️⃣ TRANSFERÊNCIA
    // =====================================================
    @Transactional
    public TransferResponseDTO transfer(
            BankAccount fromAccount,
            BankAccount toAccount,
            BigDecimal amount,
            BigDecimal fee,
            String description
    ) {
        // 1️⃣ Debita origem com taxa
        debit(fromAccount, amount, fee, description);

        // 2️⃣ Credita destino
        credit(toAccount, amount, description);

        // 3️⃣ Retorna DTO
        return new TransferResponseDTO(
                fromAccount.getId(),
                fromAccount.getBalance(),
                toAccount.getId(),
                toAccount.getBalance(),
                amount,
                fee
        );
    }

    // =====================================================
    // 4️⃣ MÉTODOS AUXILIARES
    // =====================================================
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "O valor deve ser maior que zero.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private TransactionResponseDTO buildResponse(Transaction transaction, Long accountId) {
        return new TransactionResponseDTO(
                transaction.getId(),
                accountId,
                transaction.getType(),
                transaction.getAmount(),
                transaction.getAppliedTax(),
                transaction.getCreatedAt()
        );
    }
    
    public BankAccount findAccountById(Long accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + accountId));
    }
}