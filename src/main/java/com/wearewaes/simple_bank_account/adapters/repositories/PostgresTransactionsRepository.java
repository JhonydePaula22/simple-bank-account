package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresTransactionsRepository implements TransactionsRepository {

    private final SpringJDBCTransactionsRepository springJDBCTransactionsRepository;

    public PostgresTransactionsRepository(SpringJDBCTransactionsRepository springJDBCTransactionsRepository) {
        this.springJDBCTransactionsRepository = springJDBCTransactionsRepository;
    }

    @Override
    public TransactionEntity depositMoneyIntoAccount(TransactionEntity transactionEntity) {
        return springJDBCTransactionsRepository.save(transactionEntity);
    }
}
