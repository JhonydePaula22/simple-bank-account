package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;

public class TransactionsService {

    private final TransactionsRepository transactionsRepository;

    public TransactionsService(TransactionsRepository transactionsRepository) {
        this.transactionsRepository = transactionsRepository;
    }
}
