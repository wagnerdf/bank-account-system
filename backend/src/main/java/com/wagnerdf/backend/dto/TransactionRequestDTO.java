package com.wagnerdf.backend.dto;

import com.wagnerdf.backend.enums.TransactionType;
import java.math.BigDecimal;

public record TransactionRequestDTO(
	    Long accountId,
	    TransactionType type,
	    BigDecimal amount
	) {}
