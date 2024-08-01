package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;

public interface AccountHoldersRepository {

    AccountHolderEntity saveAccountHolder(AccountHolderEntity accountHolderEntity);

    int countByIdentification(String identification);
}
