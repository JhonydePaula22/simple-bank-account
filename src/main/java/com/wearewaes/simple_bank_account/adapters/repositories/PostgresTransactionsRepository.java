package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresTransactionsRepository implements TransactionsRepository {

    private final SpringJPATransactionsRepository springJPATransactionsRepository;

    public PostgresTransactionsRepository(SpringJPATransactionsRepository springJPATransactionsRepository) {
        this.springJPATransactionsRepository = springJPATransactionsRepository;
    }

    @Override
    public TransactionEntity depositMoneyIntoAccount(TransactionEntity transactionEntity) {
        return springJPATransactionsRepository.save(transactionEntity);
    }
}
