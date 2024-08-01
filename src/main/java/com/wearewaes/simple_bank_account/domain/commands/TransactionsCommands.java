package com.wearewaes.simple_bank_account.domain.commands;

import com.wearewaes.simple_bank_account.domain.services.TransactionsService;

public class TransactionsCommands {

    private final TransactionsService transactionsService;

    public TransactionsCommands(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }
}
