package com.crm.customer.web;

import com.crm.customer.dto.CustomerDTO;
import com.crm.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> findAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @GetMapping("/{customerRefNo}")
    public ResponseEntity<CustomerDTO> findByCustomerRefNo(@PathVariable String customerRefNo) {
        return ResponseEntity.ok(customerService.findByCustomerRefNo(customerRefNo));
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(dto));
    }

    @PutMapping("/{customerRefNo}")
    public ResponseEntity<CustomerDTO> update(@PathVariable String customerRefNo,
                                              @Valid @RequestBody CustomerDTO dto) {
        return ResponseEntity.ok(customerService.update(customerRefNo, dto));
    }

    @DeleteMapping("/{customerRefNo}")
    public ResponseEntity<Void> delete(@PathVariable String customerRefNo) {
        customerService.delete(customerRefNo);
        return ResponseEntity.noContent().build();
    }
}