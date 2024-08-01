package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface SpringJPAAccountsPaginatedRepository extends PagingAndSortingRepository<AccountEntity, UUID> {
}
