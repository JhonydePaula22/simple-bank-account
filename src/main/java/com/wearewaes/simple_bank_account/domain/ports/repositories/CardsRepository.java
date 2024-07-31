package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.simple_bank_account.domain.model.CardEntity;

public interface CardsRepository {

    CardEntity saveCard(CardEntity cardEntity);
}
