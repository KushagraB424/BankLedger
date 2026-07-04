package com.kushagragupta.bankledger.repository;

import com.kushagragupta.bankledger.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountNumber(String accountNumber);
    java.util.List<Account> findByCustomerId(UUID customerId);
    boolean existsByAccountNumber(String accountNumber);
}
