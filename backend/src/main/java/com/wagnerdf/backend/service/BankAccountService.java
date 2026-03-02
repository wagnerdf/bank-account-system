package com.wagnerdf.backend.service;

import com.wagnerdf.backend.enums.AccountStatus;
import com.wagnerdf.backend.enums.AccountType;
import com.wagnerdf.backend.enums.Role;
import com.wagnerdf.backend.exception.BusinessException;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.model.UserAccount;
import com.wagnerdf.backend.repository.BankAccountRepository;
import com.wagnerdf.backend.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserAccountRepository userAccountRepository;

    // =====================================================
    // Criar conta para um usuário
    // =====================================================
    @Transactional
    public BankAccount createAccountForUser(Long userId, Role requesterRole) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado: " + userId, HttpStatus.NOT_FOUND));

        // Usuário comum só pode criar conta para si mesmo
        if (requesterRole == Role.USER && !user.getRole().equals(Role.USER)) {
            throw new BusinessException(
                "Usuário não tem permissão para criar conta para outro usuário", 
                HttpStatus.FORBIDDEN
            );
        }

        // Verifica se usuário já possui conta
        bankAccountRepository.findByUser(user).ifPresent(acc -> 
            { throw new BusinessException("Usuário já possui uma conta bancária", HttpStatus.CONFLICT); }
        );

        // Cria conta
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(AccountType.CHECKING);  // Conta padrão do usuário
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(generateAccountNumber());

        return bankAccountRepository.save(account);
    }

    // =====================================================
    // Buscar conta pelo ID
    // =====================================================
    public BankAccount findAccountById(Long accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(
                        "Conta não encontrada: " + accountId,
                        HttpStatus.NOT_FOUND
                ));
    }

    // =====================================================
    // Buscar conta pelo usuário
    // =====================================================
    public Optional<BankAccount> findByUser(UserAccount user) {
        return bankAccountRepository.findByUser(user);
    }

    // =====================================================
    // Geração simples de número de conta (ex: 8 dígitos aleatórios)
    // =====================================================
    private String generateAccountNumber() {
        int number = (int) (Math.random() * 90000000) + 10000000;
        return String.valueOf(number);
    }
}