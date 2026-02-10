package com.wagnerdf.backend.enums;

public enum ServiceType {

    ACCOUNT("Serviços relacionados à conta"),
    TRANSFER("Serviços de transferência de valores"),
    WITHDRAW("Serviços de saque"),
    DEPOSIT("Serviços de depósito"),
    MAINTENANCE("Serviços de manutenção da conta");

    private final String descricao;

    ServiceType(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
