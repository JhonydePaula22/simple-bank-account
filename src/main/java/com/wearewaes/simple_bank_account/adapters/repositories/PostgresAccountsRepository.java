package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsPaginatedRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresAccountsRepository implements AccountsRepository, AccountsPaginatedRepository {

    private final SpringJDBCAccountsRepository springJDBCAccountsRepository;
    private final SpringJDBCAccountsPaginatedRepository springJDBCAccountsPaginatedRepository;

    public PostgresAccountsRepository(SpringJDBCAccountsRepository springJDBCAccountsRepository,
                                      SpringJDBCAccountsPaginatedRepository springJDBCAccountsPaginatedRepository) {
        this.springJDBCAccountsRepository = springJDBCAccountsRepository;
        this.springJDBCAccountsPaginatedRepository = springJDBCAccountsPaginatedRepository;
    }

    @Override
    public AccountEntity saveAccount(AccountEntity accountEntity) {
        return springJDBCAccountsRepository.save(accountEntity);
    }

    @Override
    public Optional<AccountEntity> findByNumber(String accountNumber) {
        return springJDBCAccountsRepository.findByNumber(accountNumber);
    }

    @Override
    public Page<AccountEntity> findAll(PageRequest pageRequest) {
        return springJDBCAccountsPaginatedRepository.findAll(pageRequest);
    }
}
