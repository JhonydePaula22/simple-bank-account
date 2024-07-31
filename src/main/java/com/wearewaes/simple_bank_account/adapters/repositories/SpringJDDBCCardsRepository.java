package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpringJDDBCCardsRepository extends CrudRepository<CardEntity, String> {
}
