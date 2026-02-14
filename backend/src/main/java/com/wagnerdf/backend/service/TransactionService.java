package com.wagnerdf.backend.service;

import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.dto.TransferResponseDTO;
import com.wagnerdf.backend.enums.TransactionType;
import com.wagnerdf.backend.enums.TransferFeeRule;
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
    // TRANSAÇÃO SIMPLES (CREDIT / DEBIT)
    // =====================================================
    @Transactional
    public TransactionResponseDTO executeTransaction(
            Long accountId,
            TransactionType type,
            BigDecimal amount
    ) {

        validateAmount(amount);

        BankAccount account = findAccount(accountId);

        if (type == TransactionType.CREDIT) {
            return processCredit(account, amount);
        }

        if (type == TransactionType.DEBIT) {
            return processDebit(account, amount);
        }

        throw new IllegalStateException("Tipo de transação não suportado");
    }

    // =====================================================
    // TRANSFERÊNCIA
    // =====================================================
    @Transactional
    public TransferResponseDTO transfer(
            Long originAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            TransferFeeRule feeRule
    ) {

        validateAmount(amount);

        if (originAccountId.equals(destinationAccountId)) {
            throw new BusinessException(
                    "Não é possível transferir para a mesma conta",
                    HttpStatus.BAD_REQUEST
            );
        }

        BankAccount origin = findAccount(originAccountId);
        BankAccount destination = findAccount(destinationAccountId);

        BigDecimal fee = feeRule.calculate(amount);
        BigDecimal totalDebit = amount.add(fee);

        if (origin.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessException(
                    "Saldo insuficiente",
                    HttpStatus.CONFLICT
            );
        }

        origin.setBalance(origin.getBalance().subtract(totalDebit));
        destination.setBalance(destination.getBalance().add(amount));

        bankAccountRepository.save(origin);
        bankAccountRepository.save(destination);

        // DEBIT
        Transaction debitTransaction = Transaction.builder()
                .account(origin)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .appliedTax(fee)
                .build();

        // CREDIT
        Transaction creditTransaction = Transaction.builder()
                .account(destination)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .appliedTax(BigDecimal.ZERO)
                .build();

        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);

        return new TransferResponseDTO(
                origin.getId(),
                origin.getBalance(),
                destination.getId(),
                destination.getBalance(),
                amount,
                fee
        );
    }


    // =====================================================
    // CREDIT
    // =====================================================
    private TransactionResponseDTO processCredit(BankAccount account, BigDecimal amount) {

        account.setBalance(account.getBalance().add(amount));
        bankAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .appliedTax(BigDecimal.ZERO)
                .build();

        return buildResponse(transactionRepository.save(transaction));
    }

    // =====================================================
    // DEBIT
    // =====================================================
    private TransactionResponseDTO processDebit(BankAccount account, BigDecimal amount) {

        BigDecimal appliedTax = calculateTax(amount);
        BigDecimal totalDebit = amount.add(appliedTax);

        if (account.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessException(
                    "Saldo insuficiente",
                    HttpStatus.CONFLICT
            );
        }

        account.setBalance(account.getBalance().subtract(totalDebit));
        bankAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .appliedTax(appliedTax)
                .build();

        return buildResponse(transactionRepository.save(transaction));
    }

    // =====================================================
    // UTILITÁRIOS
    // =====================================================

    private BankAccount findAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Conta não encontrada",
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "O valor deve ser maior que zero.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private TransactionResponseDTO buildResponse(Transaction transaction) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getAppliedTax(),
                transaction.getCreatedAt()
        );
    }

    private BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.02"));
    }
}
