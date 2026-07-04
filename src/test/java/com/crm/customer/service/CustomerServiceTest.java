package com.crm.customer.service;

import com.crm.customer.domain.Customer;
import com.crm.customer.domain.CustomerStatus;
import com.crm.customer.dto.CustomerDTO;
import com.crm.customer.mapper.CustomerMapper;
import com.crm.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("0501234567");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setCustomerRefNo("CUST-1");

        customerDTO = new CustomerDTO(
                null,
                "CUST-1",
                "John",
                "Doe",
                "john.doe@example.com",
                "0501234567",
                CustomerStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "test",
                "test"
        );
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllCustomers() {
        given(customerRepository.findAll()).willReturn(List.of(customer));
        given(customerMapper.toDTO(customer)).willReturn(customerDTO);

        List<CustomerDTO> result = customerService.findAll();

        assertThat(result).hasSize(1).contains(customerDTO);
    }

    @Test
    void findAll_returnsEmptyList_whenNoCustomers() {
        given(customerRepository.findAll()).willReturn(List.of());

        List<CustomerDTO> result = customerService.findAll();

        assertThat(result).isEmpty();
    }

    // ── findByCustomerRefNo ──────────────────────────────────────────────────

    @Test
    void findByCustomerRefNo_returnsCustomer_whenFound() {
        given(customerRepository.findByCustomerRefNo("CUST-1")).willReturn(Optional.of(customer));
        given(customerMapper.toDTO(customer)).willReturn(customerDTO);

        CustomerDTO result = customerService.findByCustomerRefNo("CUST-1");

        assertThat(result).isEqualTo(customerDTO);
    }

    @Test
    void findByCustomerRefNo_throwsNoSuchElementException_whenNotFound() {
        given(customerRepository.findByCustomerRefNo("CUST-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findByCustomerRefNo("CUST-999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("CUST-999");
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_returnsCreatedCustomer_whenEmailIsUnique() {
        given(customerRepository.existsByEmail("john.doe@example.com")).willReturn(false);
        given(customerMapper.toEntity(customerDTO)).willReturn(customer);
        given(customerRepository.save(customer)).willReturn(customer);
        given(customerMapper.toDTO(customer)).willReturn(customerDTO);

        CustomerDTO result = customerService.create(customerDTO);

        assertThat(result).isEqualTo(customerDTO);
        then(customerRepository).should().save(customer);
    }

    @Test
    void create_throwsIllegalStateException_whenEmailAlreadyExists() {
        given(customerRepository.existsByEmail("john.doe@example.com")).willReturn(true);

        assertThatThrownBy(() -> customerService.create(customerDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("john.doe@example.com");

        then(customerRepository).should(never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_returnsUpdatedCustomer_whenValidRequest() {
        CustomerDTO updatedDTO = new CustomerDTO(
                null, "CUST-1", "Jane", "Doe", "john.doe@example.com",
                "0509999999", CustomerStatus.INACTIVE, LocalDateTime.now(), LocalDateTime.now() ,"test","test"
        );
        given(customerRepository.findByCustomerRefNo("CUST-1")).willReturn(Optional.of(customer));
        given(customerRepository.save(customer)).willReturn(customer);
        given(customerMapper.toDTO(customer)).willReturn(updatedDTO);

        CustomerDTO result = customerService.update("CUST-1", updatedDTO);

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.status()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void update_throwsNoSuchElementException_whenCustomerNotFound() {
        given(customerRepository.findByCustomerRefNo("CUST-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.update("CUST-999", customerDTO))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("CUST-999");

        then(customerRepository).should(never()).save(any());
    }

    @Test
    void update_throwsIllegalStateException_whenEmailTakenByAnotherCustomer() {
        CustomerDTO dtoWithNewEmail = new CustomerDTO(
                null, "CUST-1", "John", "Doe", "other@example.com",
                "0501234567", CustomerStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(),"test","test"
        );
        given(customerRepository.findByCustomerRefNo("CUST-1")).willReturn(Optional.of(customer));
        given(customerRepository.existsByEmailAndCustomerRefNoNot("other@example.com", "CUST-1")).willReturn(true);

        assertThatThrownBy(() -> customerService.update("CUST-1", dtoWithNewEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("other@example.com");

        then(customerRepository).should(never()).save(any());
    }

    @Test
    void update_allowsSameEmail_whenEmailIsUnchanged() {
        given(customerRepository.findByCustomerRefNo("CUST-1")).willReturn(Optional.of(customer));
        given(customerRepository.save(customer)).willReturn(customer);
        given(customerMapper.toDTO(customer)).willReturn(customerDTO);

        CustomerDTO result = customerService.update("CUST-1", customerDTO);

        assertThat(result).isEqualTo(customerDTO);
        then(customerRepository).should(never()).existsByEmailAndCustomerRefNoNot(any(), any());
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_deletesCustomer_whenFound() {
        given(customerRepository.findByCustomerRefNo("CUST-1")).willReturn(Optional.of(customer));

        customerService.delete("CUST-1");

        then(customerRepository).should().delete(customer);
    }

    @Test
    void delete_throwsNoSuchElementException_whenNotFound() {
        given(customerRepository.findByCustomerRefNo("CUST-999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete("CUST-999"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("CUST-999");

        then(customerRepository).should(never()).delete(any());
    }
}