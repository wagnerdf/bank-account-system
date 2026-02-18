package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.dto.*;
import com.wagnerdf.backend.service.StatementService;
import com.wagnerdf.backend.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final StatementService statementService;

    // =========================================
    // 1️⃣ CREDIT / DEBIT
    // =========================================
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> executeTransaction(
            @Valid @RequestBody TransactionRequestDTO request
    ) {

        // 1️⃣ Executa transação simples
        TransactionResponseDTO response =
                transactionService.executeTransaction(
                        request.accountId(),
                        request.type(),
                        request.amount()
                );

        // 2️⃣ Retorna resposta
        return ResponseEntity.ok(response);
    }

    // =========================================
    // 2️⃣ TRANSFERÊNCIA
    // =========================================
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(
            @Valid @RequestBody TransferRequestDTO request
    ) {

        // 1️⃣ Executa transferência
        TransferResponseDTO response =
                transactionService.transfer(
                        request.originAccountId(),
                        request.destinationAccountId(),
                        request.amount(),
                        request.feeRule()
                );

        // 2️⃣ Retorna resposta
        return ResponseEntity.ok(response);
    }

    // =========================================
    // 3️⃣ EXTRATO (STATEMENT)
    // =========================================
    @GetMapping("/statement/{accountId}")
    public ResponseEntity<StatementResponseDTO> getStatement(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {

        // 1️⃣ Chama Service para gerar extrato
        StatementResponseDTO statement = statementService.getStatement(accountId, startDate, endDate);

        // 2️⃣ Retorna extrato JSON
        return ResponseEntity.ok(statement);
    }
}
