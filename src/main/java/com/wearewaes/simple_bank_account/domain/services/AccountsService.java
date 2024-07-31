package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.wearewaes.simple_bank_account.domain.model.mappers.AccountMappers.toAccountEntityHolder;
import static com.wearewaes.simple_bank_account.domain.model.mappers.AccountMappers.toAccountEntityMapper;
import static com.wearewaes.simple_bank_account.domain.model.mappers.AccountMappers.toDtoMapper;

public class AccountsService {

    public static final Random RANDOM = new Random();
    private final AccountsRepository accountsRepository;
    private final AccountHoldersRepository accountHoldersRepository;
    private final CardsRepository cardsRepository;

    public AccountsService(AccountsRepository accountsRepository,
                           AccountHoldersRepository accountHoldersRepository,
                           CardsRepository cardsRepository) {
        this.accountsRepository = accountsRepository;
        this.accountHoldersRepository = accountHoldersRepository;
        this.cardsRepository = cardsRepository;
    }

    @Transactional(rollbackFor = {PersistenceException.class})
    public AccountDTO createAccount(NewAccountDTO newAccountDTO) {
        AccountHolderEntity newAccountEntityHolder = toAccountEntityHolder(newAccountDTO);
        AccountHolderEntity persistedAccountHolder = accountHoldersRepository.saveAccountHolder(newAccountEntityHolder);
        AccountEntity newAccountEntity = toAccountEntityMapper(persistedAccountHolder, generateBankAccountNumber());
        AccountEntity persistedAccount = accountsRepository.saveAccount(newAccountEntity);
        List<CardEntity> cardEntities = generateCards(persistedAccount, newAccountDTO.getCreditCard());
        return toDtoMapper(persistedAccount, cardEntities);
    }

    List<CardEntity> generateCards(AccountEntity accountEntity, boolean creditCard) {
        CardEntity debitCardEntity = generateCard(accountEntity, CardTypeEnum.DEBIT);
        List<CardEntity> cards = new ArrayList<>();
        cards.add(cardsRepository.saveCard(debitCardEntity));

        if (creditCard) {
            CardEntity creditCardEntity = generateCard(accountEntity, CardTypeEnum.CREDIT);
            cards.add(cardsRepository.saveCard(creditCardEntity));
        }

        return cards;
    }

    static CardEntity generateCard(AccountEntity accountEntity, CardTypeEnum cardType) {
        return new CardEntity(
                generateCardNumber(),
                generateCVV(),
                cardType,
                accountEntity
        );
    }

    static String generateCardNumber() {
        return String.format("%14d",RANDOM.nextLong() & 0xFFFFFFFFFFFFL); // 48-bit card number
    }

    static String generateCVV() {
        // Generate a number with cvvLength digits
        int cvv = RANDOM.nextInt((int) Math.pow(10, 3));
        // Format the CVV to ensure it has the correct number of digits
        return String.format("%03d", cvv);
    }

    static String generateBankAccountNumber() {
        // Generates a 9-digit number and adds 100000000 to ensure 10 digits
        return String.format("%10d", RANDOM.nextInt(900000000) + 100000000);
    }
}
