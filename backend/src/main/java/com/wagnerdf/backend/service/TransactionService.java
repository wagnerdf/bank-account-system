package com.wagnerdf.backend.service;

import com.wagnerdf.backend.dto.CreditRequestDTO;
import com.wagnerdf.backend.dto.DebitRequestDTO;
import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.dto.TransferResponseDTO;
import com.wagnerdf.backend.enums.AccountType;
import com.wagnerdf.backend.enums.TransactionCategory;
import com.wagnerdf.backend.enums.TransactionStatus;
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
    // 1️⃣ CRÉDITO (via BankAccount)
    // =====================================================
    @Transactional
    public TransactionResponseDTO credit(
            BankAccount toAccount,
            BigDecimal amount,
            String description
    ) {
        validateAmount(amount);

        // Atualiza saldo
        toAccount.setBalance(toAccount.getBalance().add(amount));
        bankAccountRepository.save(toAccount);

        // Cria transação
        Transaction transaction = Transaction.builder()
                .toAccount(toAccount)
                .type(TransactionType.CREDIT)
                .category(TransactionCategory.DEPOSIT)
                .amount(amount)
                .appliedTax(BigDecimal.ZERO)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        transactionRepository.save(transaction);

        return buildResponse(transaction, toAccount.getId());
    }

    // =====================================================
    // 1️⃣a CRÉDITO via DTO
    // =====================================================
    @Transactional
    public TransactionResponseDTO credit(CreditRequestDTO request) {
        BankAccount toAccount = findAccountById(request.toAccountId());
        return credit(toAccount, request.amount(), request.description());
    }

    // =====================================================
    // 2️⃣ DÉBITO (via BankAccount)
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
                .category(TransactionCategory.WITHDRAW)
                .amount(amount)
                .appliedTax(fee)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .build();

        transactionRepository.save(transaction);

        return buildResponse(transaction, fromAccount.getId());
    }

    // =====================================================
    // 2️⃣a DÉBITO via DTO
    // =====================================================
    @Transactional
    public TransactionResponseDTO debit(DebitRequestDTO request) {
        BankAccount fromAccount = findAccountById(request.fromAccountId());
        BigDecimal fee = request.fee() != null ? request.fee() : BigDecimal.ZERO;
        return debit(fromAccount, request.amount(), fee, request.description());
    }

 // =====================================================
 // 3️⃣ TRANSFERÊNCIA AJUSTADA COM ENUNS E COMENTÁRIOS
 // =====================================================
 @Transactional
 public TransferResponseDTO transfer(
         BankAccount fromAccount,
         BankAccount toAccount,
         BigDecimal amount,
         BigDecimal fee,
         String description
 ) {

     // 1️⃣ Valida valor da transferência
     validateAmount(amount);

     // Calcula total que será debitado da conta de origem (valor + taxa)
     BigDecimal totalDebit = amount.add(fee);
     if (fromAccount.getBalance().compareTo(totalDebit) < 0) {
         throw new BusinessException("Saldo insuficiente", HttpStatus.CONFLICT);
     }

     // =====================================================
     // 1️⃣ DEBIT na conta de origem
     // =====================================================
     fromAccount.setBalance(fromAccount.getBalance().subtract(totalDebit));
     bankAccountRepository.save(fromAccount);

     Transaction debitTransaction = Transaction.builder()
             .fromAccount(fromAccount)
             .type(TransactionType.DEBIT)                         // Tipo financeiro: DEBIT
             .category(TransactionCategory.TRANSFER)             // Categoria: TRANSFER porque é uma transferência de usuário
             .amount(amount)                                     // Valor principal da transferência
             .appliedTax(fee)                                   // Taxa aplicada (se houver)
             .status(TransactionStatus.COMPLETED)                // Status da transação
             .description(buildDescription(TransactionType.DEBIT, description))
             .build();

     transactionRepository.save(debitTransaction);

     // =====================================================
     // 2️⃣ CREDIT na conta de destino
     // =====================================================
     toAccount.setBalance(toAccount.getBalance().add(amount));
     bankAccountRepository.save(toAccount);

     Transaction creditTransaction = Transaction.builder()
             .toAccount(toAccount)
             .type(TransactionType.CREDIT)                       // Tipo financeiro: CREDIT
             .category(TransactionCategory.TRANSFER)            // Categoria: TRANSFER pois está recebendo uma transferência
             .amount(amount)                                     // Valor recebido
             .appliedTax(BigDecimal.ZERO)                       // Sem taxa aplicada nesta movimentação
             .status(TransactionStatus.COMPLETED)
             .description(buildDescription(TransactionType.CREDIT, description))
             .build();

     transactionRepository.save(creditTransaction);

     // =====================================================
     // 3️⃣ CREDIT para a conta da plataforma (PLATFORM_FEE)
     // =====================================================
     // Aqui só ocorre se houver fee > 0
     if (fee.compareTo(BigDecimal.ZERO) > 0) {
         BankAccount platformAccount = getPlatformFeeAccount();

         platformAccount.setBalance(platformAccount.getBalance().add(fee));
         bankAccountRepository.save(platformAccount);

         Transaction feeTransaction = Transaction.builder()
                 .toAccount(platformAccount)
                 .type(TransactionType.CREDIT)                     // Tipo financeiro: CREDIT, porque estamos creditando a plataforma
                 .category(TransactionCategory.PLATFORM_FEE)       // Categoria: PLATFORM_FEE pois é a taxa do sistema
                 .amount(fee)                                     // Valor da taxa
                 .appliedTax(BigDecimal.ZERO)                     // Sem taxa extra
                 .status(TransactionStatus.COMPLETED)
                 .description(buildDescription(
                         TransactionType.CREDIT,
                         "Taxa da transferência"
                 ))
                 .build();

         transactionRepository.save(feeTransaction);
     }

     // =====================================================
     // 4️⃣ Retorna DTO com informações consolidadas
     // =====================================================
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

    public BigDecimal calculateFee(BigDecimal amount, TransferFeeRule feeRule) {
        if (feeRule == null) {
            return BigDecimal.ZERO;
        }
        return feeRule.calculate(amount);
    }

    private BankAccount getPlatformFeeAccount() {
        return bankAccountRepository
                .findByAccountType(AccountType.PLATFORM_FEE)
                .orElseThrow(() -> new RuntimeException("Platform fee account not found"));
    }

    private String buildDescription(TransactionType type, String userDescription) {
        if (userDescription == null || userDescription.isBlank()) {
            return type.name();
        }
        return type.name() + " - " + userDescription;
    }
}