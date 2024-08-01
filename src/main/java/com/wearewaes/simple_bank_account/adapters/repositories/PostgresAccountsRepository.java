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

    private final SpringJPAAccountsRepository springJPAAccountsRepository;
    private final SpringJPAAccountsPaginatedRepository springJPAAccountsPaginatedRepository;

    public PostgresAccountsRepository(SpringJPAAccountsRepository springJPAAccountsRepository,
                                      SpringJPAAccountsPaginatedRepository springJPAAccountsPaginatedRepository) {
        this.springJPAAccountsRepository = springJPAAccountsRepository;
        this.springJPAAccountsPaginatedRepository = springJPAAccountsPaginatedRepository;
    }

    @Override
    public AccountEntity saveAccount(AccountEntity accountEntity) {
        return springJPAAccountsRepository.save(accountEntity);
    }

    @Override
    public Optional<AccountEntity> findByNumber(String accountNumber) {
        return springJPAAccountsRepository.findByNumber(accountNumber);
    }

    @Override
    public Page<AccountEntity> findAll(PageRequest pageRequest) {
        return springJPAAccountsPaginatedRepository.findAll(pageRequest);
    }
}
