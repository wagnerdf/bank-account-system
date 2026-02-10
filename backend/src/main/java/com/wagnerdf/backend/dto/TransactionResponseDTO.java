package com.wagnerdf.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.wagnerdf.backend.enums.TransactionType;

public record TransactionResponseDTO(
	    Long id,
	    Long accountId,
	    TransactionType type,
	    BigDecimal amount,
	    BigDecimal appliedTax,
	    LocalDateTime createdAt
	) {}
