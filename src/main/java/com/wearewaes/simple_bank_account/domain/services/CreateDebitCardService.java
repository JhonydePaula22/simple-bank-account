package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;

public class CreateDebitCardService extends CardGenerator {

    public CreateDebitCardService(CardsRepository cardsRepository, EncryptionService encryptionService) {
        super(encryptionService, cardsRepository, CardTypeEnum.DEBIT);
    }
}
