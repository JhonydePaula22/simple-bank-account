package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;

import static com.wearewaes.simple_bank_account.domain.services.AccountsService.RANDOM;

public abstract class CardGenerator {

    private final EncryptionService encryptionService;
    private final CardsRepository cardsRepository;
    private final CardTypeEnum type;

    protected CardGenerator(EncryptionService encryptionService, CardsRepository cardsRepository, CardTypeEnum type) {
        this.encryptionService = encryptionService;
        this.cardsRepository = cardsRepository;
        this.type = type;
    }

    CardEntity generateCard(AccountEntity accountEntity) {
        CardEntity card = new CardEntity(
                generateCardNumber(),
                generateCVV(),
                this.type,
                accountEntity
        );
        return cardsRepository.saveCard(card);
    }

    String generateCardNumber() {
        return encryptionService.encrypt(String.format("%14d", RANDOM.nextLong() & 0xFFFFFFFFFFFFL).trim());
    }

    String generateCVV() {
        // Generate a number with cvvLength digits
        int cvv = RANDOM.nextInt((int) Math.pow(10, 3));
        // Format the CVV to ensure it has the correct number of digits
        return encryptionService.encrypt(String.format("%03d", cvv).trim());
    }
}
