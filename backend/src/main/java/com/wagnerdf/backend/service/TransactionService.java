package com.wagnerdf.backend.service;

import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.enums.TransactionType;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.model.Transaction;
import com.wagnerdf.backend.repository.BankAccountRepository;
import com.wagnerdf.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional
    public TransactionResponseDTO executeTransaction(
            Long accountId,
            TransactionType type,
            BigDecimal amount
    ) {

        // 1️⃣ Validações iniciais
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        // 2️⃣ Processamento por tipo
        if (type == TransactionType.CREDIT) {
            return processCredit(account, amount);
        }

        if (type == TransactionType.DEBIT) {
            return processDebit(account, amount);
        }

        throw new IllegalStateException("Tipo de transação não suportado");
    }

    // =========================
    // CREDIT
    // =========================
    private TransactionResponseDTO processCredit(BankAccount account, BigDecimal amount) {

        // Atualiza saldo
        account.setBalance(account.getBalance().add(amount));
        bankAccountRepository.save(account);

        // Cria transação
        Transaction transaction = Transaction.builder()
                .account(account)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .appliedTax(BigDecimal.ZERO)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponseDTO(
                savedTransaction.getId(),
                savedTransaction.getAccount().getId(),
                savedTransaction.getType(),
                savedTransaction.getAmount(),
                savedTransaction.getAppliedTax(),
                savedTransaction.getCreatedAt()
        );
    }

	 // =========================
	 // DEBIT
	 // =========================
	 private TransactionResponseDTO processDebit(BankAccount account, BigDecimal amount) {
	
	     // 1️⃣ Calcula imposto
	     BigDecimal appliedTax = calculateTax(amount);
	
	     // 2️⃣ Total a debitar
	     BigDecimal totalDebit = amount.add(appliedTax);
	
	     // 3️⃣ Valida saldo
	     if (account.getBalance().compareTo(totalDebit) < 0) {
	         throw new IllegalStateException("Saldo insuficiente");
	     }
	
	     // 4️⃣ Atualiza saldo
	     account.setBalance(account.getBalance().subtract(totalDebit));
	     bankAccountRepository.save(account);
	
	     // 5️⃣ Cria transação
	     Transaction transaction = Transaction.builder()
	             .account(account)
	             .type(TransactionType.DEBIT)
	             .amount(amount)
	             .appliedTax(appliedTax)
	             .build();
	
	     Transaction savedTransaction = transactionRepository.save(transaction);
	
	     // 6️⃣ Retorna DTO
	     return new TransactionResponseDTO(
	             savedTransaction.getId(),
	             savedTransaction.getAccount().getId(),
	             savedTransaction.getType(),
	             savedTransaction.getAmount(),
	             savedTransaction.getAppliedTax(),
	             savedTransaction.getCreatedAt()
	     );
	 }
	

    // =========================
    // TAX (regra isolada)
    // =========================
    private BigDecimal calculateTax(BigDecimal amount) {
        // Regra simples inicial (ex: 2%)
        return amount.multiply(new BigDecimal("0.02"));
    }
    
}
