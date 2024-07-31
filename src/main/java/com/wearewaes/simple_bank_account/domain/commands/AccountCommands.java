package com.wearewaes.simple_bank_account.domain.commands;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;

public class AccountCommands {

    private final AccountsService accountsService;

    public AccountCommands(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    public AccountDTO createAccount(NewAccountDTO newAccountDTO) {
        return accountsService.createAccount(newAccountDTO);
    }
}
