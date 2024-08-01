package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.AccountsApi;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;
import com.wearewaes.simple_bank_account.domain.services.GetAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountsController extends ControllerAdvice implements AccountsApi {

    private final AccountsService accountsService;
    private final GetAccountService getAccountService;

    public AccountsController(AccountsService accountsService, GetAccountService getAccountService) {
        this.accountsService = accountsService;
        this.getAccountService = getAccountService;
    }

    @Override
    public ResponseEntity<AccountDTO> createAccount(NewAccountDTO newAccountDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountsService.createAccount(newAccountDTO));
    }

    @Override
    public ResponseEntity<AccountDTO> getAccount(String accountNumber) {
        return ResponseEntity.ok(getAccountService.getAccount(accountNumber));
    }

    @Override
    public ResponseEntity<AccountsBalanceDTO> getAllAccountsBalance(Integer offset, Integer limit) {
        return ResponseEntity.ok(getAccountService.getAllAccountsBalance(offset, limit));
    }
}
