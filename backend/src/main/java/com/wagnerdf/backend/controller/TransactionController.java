package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.StatementPrinter;
import com.wagnerdf.backend.dto.CreditRequestDTO;
import com.wagnerdf.backend.dto.DebitRequestDTO;
import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.dto.TransferRequestDTO;
import com.wagnerdf.backend.dto.TransferResponseDTO;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.service.StatementService;
import com.wagnerdf.backend.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final StatementService statementService;

    // =========================================
    // 1️⃣ CREDIT
    // =========================================
    @PostMapping("/credit")
    public ResponseEntity<TransactionResponseDTO> credit(@RequestBody CreditRequestDTO request) {
        BankAccount toAccount = transactionService.findAccountById(request.toAccountId());

        TransactionResponseDTO response =
                transactionService.credit(toAccount, request.amount(), request.description());

        return ResponseEntity.ok(response);
    }

    // =========================================
    // 2️⃣ DEBIT
    // =========================================
    @PostMapping("/debit")
    public ResponseEntity<TransactionResponseDTO> debit(@RequestBody DebitRequestDTO request) {
        BankAccount fromAccount = transactionService.findAccountById(request.fromAccountId());

        BigDecimal fee = request.fee() != null ? request.fee() : BigDecimal.ZERO;

        TransactionResponseDTO response =
                transactionService.debit(fromAccount, request.amount(), fee, request.description());

        return ResponseEntity.ok(response);
    }

    // =========================================
    // 3️⃣ TRANSFER
    // =========================================
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(@RequestBody @Valid TransferRequestDTO request) {
        BankAccount fromAccount = transactionService.findAccountById(request.originAccountId());
        BankAccount toAccount = transactionService.findAccountById(request.destinationAccountId());

        BigDecimal fee = transactionService.calculateFee(request.amount(), request.feeRule());

        TransferResponseDTO response = transactionService.transfer(
                fromAccount, toAccount, request.amount(), fee, "Transferência"
        );

        return ResponseEntity.ok(response);
    }

    // =========================================
    // 4️⃣ EXTRATO (STATEMENT)
    // =========================================
    @GetMapping("/statement/{accountId}")
    public ResponseEntity<StatementResponseDTO> getStatement(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // 1️⃣ Gerar extrato via service
        StatementResponseDTO statement = statementService.getStatement(accountId, startDate, endDate);

        // 2️⃣ Imprimir extrato colorido no console
        StatementPrinter.printFancyStatement(statement);

        // 3️⃣ Retornar JSON normalmente
        return ResponseEntity.ok(statement);
    }
}