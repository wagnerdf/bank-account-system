package com.wagnerdf.backend.repository;

import com.wagnerdf.backend.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}
