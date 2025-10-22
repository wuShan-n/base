package com.example.base.auth.service;

import com.example.base.auth.dto.AccountCreateRequest;
import com.example.base.auth.dto.AccountDto;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    AccountDto createAccount(Long userId, AccountCreateRequest request);

    void deleteAccount(Long userId, Long accountId);

    List<AccountDto> listAccountsByUserId(Long userId);

    Optional<AccountDto> findByIdentity(Long tenantId, String identityType, String identifier);
}
