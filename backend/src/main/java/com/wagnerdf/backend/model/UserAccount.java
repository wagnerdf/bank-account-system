package com.wagnerdf.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.wagnerdf.backend.enums.Role;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankAccount> accounts;

    // ================================
    // Construtor customizado para inicialização segura
    // ================================
    @Builder
    public UserAccount(Long id, String fullName, String email, String password, Role role, boolean active, List<BankAccount> accounts) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
        this.accounts = accounts != null ? accounts : new ArrayList<>();
    }

    // Opcional: método para garantir active padrão ao criar via JPA
    @PrePersist
    private void prePersist() {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
    }
}