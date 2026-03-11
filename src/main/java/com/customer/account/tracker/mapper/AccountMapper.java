package com.customer.account.tracker.mapper;

import com.customer.account.tracker.dto.AccountDto;
import com.customer.account.tracker.dto.AccountsResponse;
import com.customer.account.tracker.dto.CreateAccountRequest;
import com.customer.account.tracker.entity.Account;
import com.customer.account.tracker.entity.Customer;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(imports = {LocalDateTime.class})
public interface AccountMapper {

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", source = "accountRequest.openingBalance")
    @Mapping(target = "currency", source = "accountRequest.currency")
    @Mapping(target = "accountOpeningDate", expression = "java(LocalDateTime.now())")
    @Mapping(target = "accountActive", expression = "java(true)")
    @Mapping(target = "mobileNumber", source = "accountRequest.mobileNumber")
    Account mapAccountRequestToAccount(CreateAccountRequest accountRequest, Customer customer);

    @AfterMapping
    default void mapCustomer(@MappingTarget Account account, Customer customer) {
        account.setCustomer(customer);
    }

    @IterableMapping(qualifiedByName = "mapAccountToAccountDto")
    List<AccountDto> mapAccountsToAccountsDto(List<Account> account);

    @Named("mapAccountToAccountDto")
    AccountDto mapAccountToAccountDto(Account account);

    default AccountsResponse mapAccountsToAccountResponse(List<Account> accounts) {
        AccountsResponse response = new AccountsResponse();
        response.setAccounts(mapAccountsToAccountsDto(accounts));
        return response;
    }
}