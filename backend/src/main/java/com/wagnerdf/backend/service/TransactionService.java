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
    // 1️⃣ TRANSAÇÃO SIMPLES (CREDIT / DEBIT)
    // =====================================================
    @Transactional
    public TransactionResponseDTO executeTransaction(
            Long accountId,
            TransactionType type,
            BigDecimal amount
    ) {

        // 1️⃣ Validar valor da transação
        validateAmount(amount);

        // 2️⃣ Buscar conta no banco
        BankAccount account = findAccount(accountId);

        // 3️⃣ Processar transação de acordo com tipo
        if (type == TransactionType.CREDIT) {
            return processCredit(account, amount);
        }

        if (type == TransactionType.DEBIT) {
            return processDebit(account, amount);
        }

        // 4️⃣ Caso tipo não suportado
        throw new IllegalStateException("Tipo de transação não suportado");
    }

    // =====================================================
    // 2️⃣ TRANSFERÊNCIA ENTRE CONTAS
    // =====================================================
    @Transactional
    public TransferResponseDTO transfer(
            Long originAccountId,
            Long destinationAccountId,
            BigDecimal amount,
            TransferFeeRule feeRule
    ) {

        // 1️⃣ Validar valor
        validateAmount(amount);

        // 2️⃣ Impedir transferência para a mesma conta
        if (originAccountId.equals(destinationAccountId)) {
            throw new BusinessException(
                    "Não é possível transferir para a mesma conta",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3️⃣ Buscar contas de origem e destino
        BankAccount origin = findAccount(originAccountId);
        BankAccount destination = findAccount(destinationAccountId);

        // 4️⃣ Calcular taxa e valor total debitado
        BigDecimal fee = feeRule.calculate(amount);
        BigDecimal totalDebit = amount.add(fee);

        // 5️⃣ Validar saldo suficiente na conta de origem
        if (origin.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessException(
                    "Saldo insuficiente",
                    HttpStatus.CONFLICT
            );
        }

        // 6️⃣ Atualizar saldos das contas
        origin.setBalance(origin.getBalance().subtract(totalDebit));
        destination.setBalance(destination.getBalance().add(amount));

        bankAccountRepository.save(origin);
        bankAccountRepository.save(destination);

        // 7️⃣ Criar transações
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

        // 8️⃣ Montar DTO de resposta
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
    // 3️⃣ PROCESSAR CRÉDITO
    // =====================================================
    private TransactionResponseDTO processCredit(BankAccount account, BigDecimal amount) {

        // 1️⃣ Atualizar saldo da conta
        account.setBalance(account.getBalance().add(amount));
        bankAccountRepository.save(account);

        // 2️⃣ Criar transação
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .appliedTax(BigDecimal.ZERO)
                .build();

        // 3️⃣ Montar DTO de resposta
        return buildResponse(transactionRepository.save(transaction));
    }

    // =====================================================
    // 4️⃣ PROCESSAR DÉBITO
    // =====================================================
    private TransactionResponseDTO processDebit(BankAccount account, BigDecimal amount) {

        // 1️⃣ Calcular taxa e total a debitar
        BigDecimal appliedTax = calculateTax(amount);
        BigDecimal totalDebit = amount.add(appliedTax);

        // 2️⃣ Validar saldo suficiente
        if (account.getBalance().compareTo(totalDebit) < 0) {
            throw new BusinessException(
                    "Saldo insuficiente",
                    HttpStatus.CONFLICT
            );
        }

        // 3️⃣ Atualizar saldo da conta
        account.setBalance(account.getBalance().subtract(totalDebit));
        bankAccountRepository.save(account);

        // 4️⃣ Criar transação
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .appliedTax(appliedTax)
                .build();

        // 5️⃣ Montar DTO de resposta
        return buildResponse(transactionRepository.save(transaction));
    }

    // =====================================================
    // 5️⃣ MÉTODOS AUXILIARES
    // =====================================================

    // Buscar conta
    private BankAccount findAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Conta não encontrada",
                        HttpStatus.NOT_FOUND
                ));
    }

    // Validar valor da transação
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "O valor deve ser maior que zero.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Montar DTO de transação
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

    // Calcular taxa de débito
    private BigDecimal calculateTax(BigDecimal amount) {
        return amount.multiply(new BigDecimal("0.02"));
    }
}
