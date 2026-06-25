package com.crm.customer.web;

import com.crm.customer.domain.CustomerStatus;
import com.crm.customer.dto.CustomerDTO;
import com.crm.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private CustomerDTO customerDTO;
    private CustomerDTO requestDTO;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO(
                null,
                "CUST-1",
                "John",
                "Doe",
                "john.doe@example.com",
                "0501234567",
                CustomerStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        requestDTO = new CustomerDTO(
                null,
                null,
                "John",
                "Doe",
                "john.doe@example.com",
                "0501234567",
                CustomerStatus.ACTIVE,
                null,
                null
        );
    }

    // ── GET /api/customers ───────────────────────────────────────────────────

    @Test
    void findAll_returns200WithCustomerList() throws Exception {
        given(customerService.findAll()).willReturn(List.of(customerDTO));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerRefNo").value("CUST-1"))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void findAll_returns200WithEmptyList_whenNoCustomers() throws Exception {
        given(customerService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/customers/{customerRefNo} ───────────────────────────────────

    @Test
    void findByCustomerRefNo_returns200_whenFound() throws Exception {
        given(customerService.findByCustomerRefNo("CUST-1")).willReturn(customerDTO);

        mockMvc.perform(get("/api/customers/CUST-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRefNo").value("CUST-1"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void findByCustomerRefNo_returns404_whenNotFound() throws Exception {
        given(customerService.findByCustomerRefNo("CUST-999"))
                .willThrow(new NoSuchElementException("Customer not found: CUST-999"));

        mockMvc.perform(get("/api/customers/CUST-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: CUST-999"));
    }

    // ── POST /api/customers ──────────────────────────────────────────────────

    @Test
    void create_returns201WithCreatedCustomer_whenValidRequest() throws Exception {
        given(customerService.create(any(CustomerDTO.class))).willReturn(customerDTO);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerRefNo").value("CUST-1"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void create_returns400_whenRequiredFieldsMissing() throws Exception {
        CustomerDTO invalidDTO = new CustomerDTO(null, null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").value("First name is required"))
                .andExpect(jsonPath("$.errors.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
                .andExpect(jsonPath("$.errors.status").value("Status is required"));
    }

    @Test
    void create_returns400_whenEmailIsInvalid() throws Exception {
        CustomerDTO invalidDTO = new CustomerDTO(
                null, null, "John", "Doe", "not-an-email",
                null, CustomerStatus.ACTIVE, null, null
        );

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email must be valid"));
    }

    @Test
    void create_returns409_whenEmailAlreadyExists() throws Exception {
        given(customerService.create(any(CustomerDTO.class)))
                .willThrow(new IllegalStateException("Email already in use: john.doe@example.com"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use: john.doe@example.com"));
    }

    // ── PUT /api/customers/{customerRefNo} ───────────────────────────────────

    @Test
    void update_returns200WithUpdatedCustomer_whenValidRequest() throws Exception {
        given(customerService.update(eq("CUST-1"), any(CustomerDTO.class))).willReturn(customerDTO);

        mockMvc.perform(put("/api/customers/CUST-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRefNo").value("CUST-1"));
    }

    @Test
    void update_returns404_whenCustomerNotFound() throws Exception {
        given(customerService.update(eq("CUST-999"), any(CustomerDTO.class)))
                .willThrow(new NoSuchElementException("Customer not found: CUST-999"));

        mockMvc.perform(put("/api/customers/CUST-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: CUST-999"));
    }

    @Test
    void update_returns409_whenEmailTakenByAnotherCustomer() throws Exception {
        given(customerService.update(eq("CUST-1"), any(CustomerDTO.class)))
                .willThrow(new IllegalStateException("Email already in use: other@example.com"));

        mockMvc.perform(put("/api/customers/CUST-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use: other@example.com"));
    }

    // ── DELETE /api/customers/{customerRefNo} ────────────────────────────────

    @Test
    void delete_returns204_whenCustomerDeleted() throws Exception {
        willDoNothing().given(customerService).delete("CUST-1");

        mockMvc.perform(delete("/api/customers/CUST-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404_whenCustomerNotFound() throws Exception {
        willThrow(new NoSuchElementException("Customer not found: CUST-999"))
                .given(customerService).delete("CUST-999");

        mockMvc.perform(delete("/api/customers/CUST-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: CUST-999"));
    }
}