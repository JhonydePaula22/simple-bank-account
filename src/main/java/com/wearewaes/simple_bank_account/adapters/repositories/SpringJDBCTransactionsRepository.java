package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SpringJDBCTransactionsRepository extends CrudRepository<TransactionEntity, UUID> {
}
