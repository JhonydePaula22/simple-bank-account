package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpringJDDBCCardsRepository extends CrudRepository<CardEntity, String> {

    List<CardEntity> findByAccount(AccountEntity accountEntity);
}
