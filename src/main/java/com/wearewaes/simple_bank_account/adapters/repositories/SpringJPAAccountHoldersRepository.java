package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SpringJPAAccountHoldersRepository extends CrudRepository<AccountHolderEntity, UUID> {

    int countByIdentification(String identification);
}
