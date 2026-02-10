package com.wagnerdf.backend.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.wagnerdf.backend.enums.ServiceType;

@Entity
@Table(name = "service_fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentual;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(nullable = false)
    private Boolean ativo = true;
}
