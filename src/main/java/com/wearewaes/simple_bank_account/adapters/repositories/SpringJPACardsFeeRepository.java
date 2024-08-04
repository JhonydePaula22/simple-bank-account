package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.CardFeeEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpringJPACardsFeeRepository extends CrudRepository<CardFeeEntity, CardTypeEnum> {
}
