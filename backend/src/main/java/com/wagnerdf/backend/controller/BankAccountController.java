package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.enums.Role;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.service.BankAccountService;
import com.wagnerdf.backend.model.UserAccount;
import com.wagnerdf.backend.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final UserService userService; // assume serviço para obter usuário logado

    // =====================================================
    // Criar conta para o usuário logado (USER)
    // =====================================================
    @PostMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BankAccount> createMyAccount() {
        // 1️⃣ Pega o usuário logado a partir do token
        UserAccount loggedUser = userService.getLoggedUser(); // retorna UserAccount

        // 2️⃣ Garante que o tipo do role seja o enum Role do sistema
        Role requesterRole = Role.valueOf(loggedUser.getRole().getName());

        // 3️⃣ Chama o service para criar a conta
        BankAccount account = bankAccountService.createAccountForUser(
                loggedUser.getId(), 
                requesterRole
        );

        // 4️⃣ Retorna a conta criada
        return ResponseEntity.ok(account);
    }

    // =====================================================
    // Criar conta para outro usuário (ADMIN)
    // =====================================================
    @PostMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccount> createAccountForUser(@PathVariable Long userId) {
        BankAccount account = bankAccountService.createAccountForUser(userId, Role.ADMIN);
        return ResponseEntity.ok(account);
    }

    // =====================================================
    // Buscar conta pelo ID
    // =====================================================
    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BankAccount> getAccount(@PathVariable Long accountId) {
        BankAccount account = bankAccountService.findAccountById(accountId);
        return ResponseEntity.ok(account);
    }
}