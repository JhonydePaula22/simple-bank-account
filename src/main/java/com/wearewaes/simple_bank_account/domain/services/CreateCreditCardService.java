package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;

public class CreateCreditCardService extends CardGenerator {

    public CreateCreditCardService(CardsRepository cardsRepository, EncryptionService encryptionService) {
        super(encryptionService, cardsRepository, CardTypeEnum.CREDIT);
    }
}
