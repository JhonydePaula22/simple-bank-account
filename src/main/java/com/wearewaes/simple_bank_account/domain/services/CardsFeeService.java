package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardFeeDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.CardFeeEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsFeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;

public class CardsFeeService {

    private final CardsFeeRepository cardsFeeRepository;

    public CardsFeeService(CardsFeeRepository cardsFeeRepository) {
        this.cardsFeeRepository = cardsFeeRepository;
    }

    @Cacheable(value = "cardsFee", key = "#cardTypeEnum")
    public BigDecimal getCardFee(CardTypeEnum cardTypeEnum) {
        return cardsFeeRepository.getFeeByType(cardTypeEnum).getFee();
    }

    @CacheEvict(value = "cardsFee", key = "#cardFeeDTO.type")
    public CardFeeDTO updateCardFee(CardFeeDTO cardFeeDTO){
        CardFeeEntity cardFeeEntity = new CardFeeEntity(cardFeeDTO.getType(), BigDecimal.valueOf(cardFeeDTO.getFee()));
        cardsFeeRepository.save(cardFeeEntity);
        return cardFeeDTO;
    }
}
