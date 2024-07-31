package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface AccountsPaginatedRepository {

    Page<AccountEntity> findAll(PageRequest pageRequest);
}
