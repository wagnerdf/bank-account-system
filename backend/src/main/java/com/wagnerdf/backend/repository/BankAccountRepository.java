package com.wagnerdf.backend.repository;

import com.wagnerdf.backend.model.BankAccount;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	
	@Query("SELECT b FROM BankAccount b JOIN FETCH b.user WHERE b.id = :id")
	Optional<BankAccount> findByIdWithUser(@Param("id") Long id);


}
