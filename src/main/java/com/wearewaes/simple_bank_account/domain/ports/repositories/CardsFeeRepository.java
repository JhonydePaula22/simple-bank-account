package com.wearewaes.simple_bank_account.domain.ports.repositories;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.CardFeeEntity;

public interface CardsFeeRepository {

    CardFeeEntity save(CardFeeEntity cardFeeEntity);

    CardFeeEntity getFeeByType(CardTypeEnum cardTypeEnum);
}
