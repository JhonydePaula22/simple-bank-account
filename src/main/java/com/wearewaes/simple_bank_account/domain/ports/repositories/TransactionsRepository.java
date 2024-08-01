package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;

public interface TransactionsRepository {

    TransactionEntity save(TransactionEntity transactionEntity);
}
