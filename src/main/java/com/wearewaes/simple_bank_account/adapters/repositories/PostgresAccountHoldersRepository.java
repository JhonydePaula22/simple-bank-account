package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresAccountHoldersRepository implements AccountHoldersRepository {

    private final SpringJDBCAccountHoldersRepository springJDBCAccountHoldersRepository;

    public PostgresAccountHoldersRepository(SpringJDBCAccountHoldersRepository springJDBCAccountHoldersRepository) {
        this.springJDBCAccountHoldersRepository = springJDBCAccountHoldersRepository;
    }

    @Override
    public AccountHolderEntity saveAccountHolder(AccountHolderEntity accountHolderEntity) {
        return springJDBCAccountHoldersRepository.save(accountHolderEntity);
    }

    @Override
    public int countByIdentification(String identification) {
        return springJDBCAccountHoldersRepository.countByIdentification(identification);
    }
}
