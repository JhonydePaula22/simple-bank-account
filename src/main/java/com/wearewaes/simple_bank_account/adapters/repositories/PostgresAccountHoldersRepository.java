package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresAccountHoldersRepository implements AccountHoldersRepository {

    private final SpringJPAAccountHoldersRepository springJPAAccountHoldersRepository;

    public PostgresAccountHoldersRepository(SpringJPAAccountHoldersRepository springJPAAccountHoldersRepository) {
        this.springJPAAccountHoldersRepository = springJPAAccountHoldersRepository;
    }

    @Override
    public AccountHolderEntity saveAccountHolder(AccountHolderEntity accountHolderEntity) {
        return springJPAAccountHoldersRepository.save(accountHolderEntity);
    }

    @Override
    public int countByIdentification(String identification) {
        return springJPAAccountHoldersRepository.countByIdentification(identification);
    }
}
