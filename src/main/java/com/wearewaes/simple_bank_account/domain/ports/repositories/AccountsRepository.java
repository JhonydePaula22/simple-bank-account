package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface AccountsRepository {

    AccountEntity saveAccount(AccountEntity accountEntity);

    Optional<AccountEntity> findByNumber(String accountNumber);

    Page<AccountEntity> findAll(PageRequest pageRequest);
}
