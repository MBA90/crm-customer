package com.crm.customer.repository;

import com.crm.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    Optional<Customer> findByCustomerRefNo(String customerRefNo);

    boolean existsByEmailAndCustomerRefNoNot(String email, String customerRefNo);
}