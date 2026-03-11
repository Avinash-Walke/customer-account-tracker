package com.customer.account.tracker.mapper;

import com.customer.account.tracker.dto.CreateAccountRequest;
import com.customer.account.tracker.dto.CustomerDetails;
import com.customer.account.tracker.dto.CustomerResponse;
import com.customer.account.tracker.dto.CustomerUpdateRequest;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.entity.Customer;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Mapper(imports = {LocalDateTime.class})
public interface CustomerMapper {

    //@Mapping(target = "id", ignore = true)
    Customer mapAccountRequestToCustomer(CreateAccountRequest accountRequest);

    @Mapping(target = "customer", ignore = true)
//    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", source = "accountRequest.openingBalance")
    @Mapping(target = "currency", source = "accountRequest.currency")
    @Mapping(target = "accountOpeningDate", expression = "java(LocalDateTime.now())")
    @Mapping(target = "accountActive", expression = "java(true)")
    @Mapping(target = "mobileNumber", source = "accountRequest.mobileNumber")
    Account mapAccountRequestToAccount(CreateAccountRequest accountRequest, Customer customer);

    @AfterMapping
    default void mapCustomer(@MappingTarget Customer customer, CreateAccountRequest accountRequest) {
        Account account = mapAccountRequestToAccount(accountRequest, customer);
        customer.addAccount(account);
    }

    @IterableMapping(qualifiedByName = "mapCustomerToCustomerDetails")
    List<CustomerDetails> mapCustomersEntityToCustomerDetails(List<Customer> customersEntity);

    default CustomerResponse mapAccountsToCustomerResponse(List<Customer> customersEntity) {
        CustomerResponse response = new CustomerResponse();
        response.setCustomers(mapCustomersEntityToCustomerDetails(customersEntity));
        return response;
    }

    @Mapping(target = "id", ignore = true)
    Customer mapCustomerUpdateRequestToCustomer(@MappingTarget Customer customer, CustomerUpdateRequest updateRequest);

    @Named("mapCustomerToCustomerDetails")
    @Mapping(target = "dob", source = "dob")
    CustomerDetails mapCustomerToCustomerDetails(Customer customer);

    default CustomerResponse mapCustomerToCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        CustomerDetails customers = mapCustomerToCustomerDetails(customer);
        response.setCustomers(Collections.singletonList((customers)));
        return response;
    }
}