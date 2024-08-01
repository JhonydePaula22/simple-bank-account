package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;

public class CreateCreditCardService implements CardGenerator {

    private final CardsRepository cardsRepository;

    public CreateCreditCardService(CardsRepository cardsRepository) {
        this.cardsRepository = cardsRepository;
    }

    @Override
    public CardEntity generateCard(AccountEntity accountEntity) {
        var card = new CardEntity(
                generateCardNumber(),
                generateCVV(),
                CardTypeEnum.CREDIT,
                accountEntity
        );
        return cardsRepository.saveCard(card);
    }
}
