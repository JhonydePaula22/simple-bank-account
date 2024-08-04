package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.CardFeeEntity;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BadRequestException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsFeeRepository;
import org.springframework.stereotype.Component;

@Component
public class PostgresCardsFeeRepository implements CardsFeeRepository {

    private final SpringJPACardsFeeRepository springJPACardsFeeRepository;

    public PostgresCardsFeeRepository(SpringJPACardsFeeRepository springJPACardsFeeRepository) {
        this.springJPACardsFeeRepository = springJPACardsFeeRepository;
    }

    @Override
    public CardFeeEntity save(CardFeeEntity cardFeeEntity) {
        return springJPACardsFeeRepository.save(cardFeeEntity);
    }

    @Override
    public CardFeeEntity getFeeByType(CardTypeEnum cardTypeEnum) {
        return springJPACardsFeeRepository.findById(cardTypeEnum)
                .orElseThrow(() -> new BadRequestException("Invalid Card Type. Please check your request!"));
    }
}
