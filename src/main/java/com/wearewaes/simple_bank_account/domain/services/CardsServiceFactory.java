package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.exceptions.InternalErrorException;

import java.util.Map;

public class CardsServiceFactory {

    private final Map<CardTypeEnum, CardGenerator> cardGenerators;

    public CardsServiceFactory(CreateDebitCardService createDebitCardService,
                               CreateCreditCardService createCreditCardService) {
        this.cardGenerators = Map.of(
                CardTypeEnum.CREDIT, createCreditCardService,
                CardTypeEnum.DEBIT, createDebitCardService);
    }

    public CardGenerator getCardGenerator(CardTypeEnum cardTypeEnum) {
        if (!cardGenerators.containsKey(cardTypeEnum)) {
            throw new InternalErrorException("Card not supported exception");
        }
        return cardGenerators.get(cardTypeEnum);
    }
}
