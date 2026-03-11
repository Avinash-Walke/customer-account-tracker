package com.customer.account.tracker.repository;

import com.customer.account.tracker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {

    boolean existsByCustomerId(Integer customerId);

}
