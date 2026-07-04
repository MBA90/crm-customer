package com.crm.customer.service;

import com.crm.customer.domain.Customer;
import com.crm.customer.dto.CustomerDTO;
import com.crm.customer.mapper.CustomerMapper;
import com.crm.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public List<CustomerDTO> findAll() {
        log.debug("Fetching all customers");
        List<CustomerDTO> customers = customerRepository.findAll().stream()
                .map(customerMapper::toDTO)
                .toList();
        log.debug("Found {} customers", customers.size());
        return customers;
    }

    public CustomerDTO findByCustomerRefNo(String customerRefNo) {
        log.debug("Fetching customer with ref: {}", customerRefNo);
        return customerRepository.findByCustomerRefNo(customerRefNo)
                .map(customerMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ref: {}", customerRefNo);
                    return new NoSuchElementException("Customer not found: " + customerRefNo);
                });
    }
 
    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        log.info("Creating customer with email: {}", dto.email());
        if (customerRepository.existsByEmail(dto.email())) {
            log.warn("Email already in use: {}", dto.email());
            throw new IllegalStateException("Email already in use: " + dto.email());
        }
        CustomerDTO created = customerMapper.toDTO(customerRepository.save(customerMapper.toEntity(dto)));
        log.info("Customer created with ref: {}", created.customerRefNo());
        return created;
    }

    @Transactional
    public CustomerDTO update(String customerRefNo, CustomerDTO dto) {
        log.info("Updating customer with ref: {}", customerRefNo);
        Customer customer = customerRepository.findByCustomerRefNo(customerRefNo)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ref: {}", customerRefNo);
                    return new NoSuchElementException("Customer not found: " + customerRefNo);
                });
        if (!customer.getEmail().equalsIgnoreCase(dto.email())
                && customerRepository.existsByEmailAndCustomerRefNoNot(dto.email(), customerRefNo)) {
            log.warn("Email already in use by another customer: {}", dto.email());
            throw new IllegalStateException("Email already in use: " + dto.email());
        }
        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setEmail(dto.email());
        customer.setPhone(dto.phone());
        customer.setStatus(dto.status());
        CustomerDTO updated = customerMapper.toDTO(customerRepository.save(customer));
        log.info("Customer updated with ref: {}", customerRefNo);
        return updated;
    }

    @Transactional
    public void delete(String customerRefNo) {
        log.info("Deleting customer with ref: {}", customerRefNo);
        Customer customer = customerRepository.findByCustomerRefNo(customerRefNo)
                .orElseThrow(() -> {
                    log.warn("Customer not found with ref: {}", customerRefNo);
                    return new NoSuchElementException("Customer not found: " + customerRefNo);
                });
        customerRepository.delete(customer);
        log.info("Customer deleted with ref: {}", customerRefNo);
    }
}