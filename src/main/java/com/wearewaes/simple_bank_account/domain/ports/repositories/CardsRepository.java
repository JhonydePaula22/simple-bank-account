package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;

import java.util.List;

public interface CardsRepository {

    CardEntity saveCard(CardEntity cardEntity);

    List<CardEntity> findCardsByAccount(AccountEntity accountEntity);
}
