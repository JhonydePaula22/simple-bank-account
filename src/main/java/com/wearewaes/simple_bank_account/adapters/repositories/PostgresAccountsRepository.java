package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresAccountsRepository implements AccountsRepository {

    private final SpringJDBCAccountsRepository springJDBCAccountsRepository;

    public PostgresAccountsRepository(SpringJDBCAccountsRepository springJDBCAccountsRepository) {
        this.springJDBCAccountsRepository = springJDBCAccountsRepository;
    }

    @Override
    public AccountEntity saveAccount(AccountEntity accountEntity) {
        return springJDBCAccountsRepository.save(accountEntity);
    }
}
