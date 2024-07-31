package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;

public interface AccountsRepository {

    AccountEntity saveAccount(AccountEntity accountEntity);
}
