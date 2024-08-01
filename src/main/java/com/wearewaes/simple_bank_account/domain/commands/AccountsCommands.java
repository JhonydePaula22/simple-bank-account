package com.wearewaes.simple_bank_account.domain.commands;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;

public class AccountsCommands {

    private final AccountsService accountsService;

    public AccountsCommands(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    public AccountDTO createAccount(NewAccountDTO newAccountDTO) {
        return accountsService.createAccount(newAccountDTO);
    }

    public AccountDTO getAccount(String accountNumber) {
        return accountsService.getAccount(accountNumber);
    }

    public AccountsBalanceDTO getAllAccountsBalance(Integer offset, Integer limit) {
        return accountsService.getAllAccountsBalance(offset, limit);
    }
}
