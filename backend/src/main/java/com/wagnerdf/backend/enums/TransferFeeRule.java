package com.wagnerdf.backend.enums;

import java.math.BigDecimal;

public enum TransferFeeRule {

    NONE(BigDecimal.ZERO, null),
    FIXED(new BigDecimal("5.00"), TransferFeeType.FIXED),
    PERCENTAGE(new BigDecimal("0.01"), TransferFeeType.PERCENTAGE); // 1%

    private final BigDecimal value;
    private final TransferFeeType type;

    TransferFeeRule(BigDecimal value, TransferFeeType type) {
        this.value = value;
        this.type = type;
    }

    public BigDecimal calculate(BigDecimal amount) {
        if (type == TransferFeeType.FIXED) {
            return value;
        }
        if (type == TransferFeeType.PERCENTAGE) {
            return amount.multiply(value);
        }
        return BigDecimal.ZERO;
    }
}
