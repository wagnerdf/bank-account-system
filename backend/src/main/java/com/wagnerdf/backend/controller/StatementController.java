package com.wagnerdf.backend.controller;

import com.wagnerdf.backend.StatementPrinter;
import com.wagnerdf.backend.dto.StatementResponseDTO;
import com.wagnerdf.backend.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    // =====================================================
    // 1️⃣ Endpoint para extrato por período
    // =====================================================
    @GetMapping("/{accountId}/statement")
    public ResponseEntity<StatementResponseDTO> getStatement(
            @PathVariable Long accountId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDateStr,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDateStr
    ) {
        LocalDate startDate = LocalDate.parse(startDateStr.trim());
        LocalDate endDate = LocalDate.parse(endDateStr.trim());
        
        // 1️⃣ Gerar DTO do extrato
        StatementResponseDTO response = statementService.getStatement(accountId, startDate, endDate);

        // 2️⃣ Imprimir no console usando a variável correta
        StatementPrinter.printFancyStatement(response);

        // 3️⃣ Retornar JSON normalmente
        return ResponseEntity.ok(response);

    }
}
