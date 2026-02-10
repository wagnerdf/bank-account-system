package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.dto.TransactionRequestDTO;
import com.wagnerdf.backend.dto.TransactionResponseDTO;
import com.wagnerdf.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> executeTransaction(
            @RequestBody TransactionRequestDTO request
    ) {
        TransactionResponseDTO response = transactionService.executeTransaction(
        	    request.accountId(),
        	    request.type(),
        	    request.amount()
        );

        return ResponseEntity.ok(response);
    }
}
