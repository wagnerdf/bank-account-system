package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.dto.CreditRequestDTO;
import com.wagnerdf.backend.dto.DebitRequestDTO;
import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.dto.TransferResponseDTO;
import com.wagnerdf.backend.model.BankAccount;
import com.wagnerdf.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

	 // =========================================
	 // 1️⃣ CREDIT
	 // =========================================
	 @PostMapping("/credit")
	 public ResponseEntity<TransactionResponseDTO> credit(
	         @RequestBody CreditRequestDTO request
	 ) {
	
	     BankAccount toAccount =
	             transactionService.findAccountById(request.toAccountId());
	
	     TransactionResponseDTO response =
	             transactionService.credit(
	                     toAccount,
	                     request.amount(),
	                     request.description()
	             );
	
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
	             transactionService.debit(
	                     fromAccount,
	                     request.amount(),
	                     fee,
	                     request.description()
	             );

	     return ResponseEntity.ok(response);
	 }

    // =========================================
    // 3️⃣ TRANSFER
    // =========================================
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(
            @RequestParam Long fromAccountId,
            @RequestParam Long toAccountId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) BigDecimal fee,
            @RequestParam(required = false) String description
    ) {
        BankAccount fromAccount = transactionService.findAccountById(fromAccountId);
        BankAccount toAccount = transactionService.findAccountById(toAccountId);

        TransferResponseDTO response =
                transactionService.transfer(
                        fromAccount,
                        toAccount,
                        amount,
                        fee != null ? fee : BigDecimal.ZERO,
                        description
                );

        return ResponseEntity.ok(response);
    }

}