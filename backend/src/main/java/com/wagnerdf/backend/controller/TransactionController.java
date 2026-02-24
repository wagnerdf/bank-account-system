package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.StatementPrinter;
import com.wagnerdf.backend.dto.CreditRequestDTO;
import com.wagnerdf.backend.dto.DebitRequestDTO;
import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.dto.TransferRequestDTO;
import com.wagnerdf.backend.dto.TransferResponseDTO;
import com.wagnerdf.backend.service.StatementService;
import com.wagnerdf.backend.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final StatementService statementService;

    // =========================================
    // 1️⃣ CREDIT
    // =========================================
    @PostMapping("/{accountId}/credit")
    public ResponseEntity<TransactionResponseDTO> credit(
            @PathVariable Long accountId,
            @RequestBody @Valid CreditRequestDTO request) {

        CreditRequestDTO newRequest =
                new CreditRequestDTO(accountId, request.amount(), request.description());

        TransactionResponseDTO response =
                transactionService.credit(newRequest);

        return ResponseEntity.ok(response);
    }

    // =========================================
    // 2️⃣ DEBIT
    // =========================================
    @PostMapping("/{accountId}/debit")
    public ResponseEntity<TransactionResponseDTO> debit(
            @PathVariable Long accountId,
            @RequestBody @Valid DebitRequestDTO request) {

        DebitRequestDTO newRequest =
                new DebitRequestDTO(accountId, request.amount(), request.fee(), request.description());

        TransactionResponseDTO response =
                transactionService.debit(newRequest);

        return ResponseEntity.ok(response);
    }

    // =========================================
    // 3️⃣ TRANSFER
    // =========================================
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(
            @RequestBody @Valid TransferRequestDTO request) {

        var fromAccount = transactionService.findAccountById(request.originAccountId());
        var toAccount = transactionService.findAccountById(request.destinationAccountId());

        var fee = transactionService.calculateFee(request.amount(), request.feeRule());

        TransferResponseDTO response = transactionService.transfer(
                fromAccount,
                toAccount,
                request.amount(),
                fee,
                "Transferência"
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
	     StatementResponseDTO statement =
	             statementService.getStatement(accountId, startDate, endDate);
	
	     // 2️⃣ Imprimir no console de forma colorida e intuitiva
	     StatementPrinter.printFancyStatement(statement);
	
	     // 3️⃣ Retornar JSON normalmente
	     return ResponseEntity.ok(statement);
	 }
}