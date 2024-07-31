package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;

import java.util.Optional;

public interface AccountsRepository {

    AccountEntity saveAccount(AccountEntity accountEntity);

    Optional<AccountEntity> findByNumber(String accountNumber);
}
