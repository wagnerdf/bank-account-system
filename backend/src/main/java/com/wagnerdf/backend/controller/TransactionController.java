package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.dto.*;
import com.wagnerdf.backend.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // =========================================
    // CREDIT / DEBIT
    // =========================================
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> executeTransaction(
            @Valid @RequestBody TransactionRequestDTO request
    ) {

        TransactionResponseDTO response =
                transactionService.executeTransaction(
                        request.accountId(),
                        request.type(),
                        request.amount()
                );

        return ResponseEntity.ok(response);
    }

    // =========================================
    // TRANSFER
    // =========================================
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDTO> transfer(
            @Valid @RequestBody TransferRequestDTO request
    ) {

        TransferResponseDTO response =
                transactionService.transfer(
                        request.originAccountId(),
                        request.destinationAccountId(),
                        request.amount(),
                        request.feeRule()
                );

        return ResponseEntity.ok(response);
    }
}
