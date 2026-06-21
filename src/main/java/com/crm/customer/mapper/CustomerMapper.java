package com.crm.customer.mapper;

import com.crm.customer.domain.Customer;
import com.crm.customer.dto.CustomerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerDTO customerDTO);

    CustomerDTO toDTO(Customer customer);
}