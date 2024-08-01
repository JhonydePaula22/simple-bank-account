package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BusinessException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.InternalErrorException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
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
    private final CardsServiceFactoryService cardsServiceFactoryService;

    public AccountsService(AccountsRepository accountsRepository,
                           AccountHoldersRepository accountHoldersRepository,
                           CardsServiceFactoryService cardsServiceFactoryService) {
        this.accountsRepository = accountsRepository;
        this.accountHoldersRepository = accountHoldersRepository;
        this.cardsServiceFactoryService = cardsServiceFactoryService;
    }

    @Transactional(rollbackFor = {PersistenceException.class})
    public AccountDTO createAccount(NewAccountDTO newAccountDTO) {
        AccountHolderEntity persistedAccountHolder = persistAccountHolder(newAccountDTO);
        AccountEntity persistedAccount = persistAccount(persistedAccountHolder);
        List<CardEntity> cardEntities = generateCards(persistedAccount, newAccountDTO.getCreditCard());
        return toDtoMapper(persistedAccount, cardEntities);
    }

    private AccountHolderEntity persistAccountHolder(NewAccountDTO newAccountDTO) {
        if (accountHoldersRepository.countByIdentification(newAccountDTO.getHolder().getId()) == 0) {
            AccountHolderEntity newAccountEntityHolder = toAccountEntityHolder(newAccountDTO);
            return accountHoldersRepository.saveAccountHolder(newAccountEntityHolder);
        }
        throw new BusinessException("Failed to persist the user. Please check your data. " +
                "If you already have an account, you may not create a new one!");
    }

    private AccountEntity persistAccount(AccountHolderEntity persistedAccountHolder) {
        try {
            AccountEntity newAccountEntity = toAccountEntityMapper(persistedAccountHolder, generateBankAccountNumber());
            return accountsRepository.saveAccount(newAccountEntity);
        } catch (Exception e) {
            throw new InternalErrorException("Failed to persist the account. Please try again in a moment!");
        }
    }

    private List<CardEntity> generateCards(AccountEntity accountEntity, boolean creditCard) {
        List<CardEntity> cards = new ArrayList<>();
        CardEntity debitCardEntity = cardsServiceFactoryService.getCardGenerator(CardTypeEnum.DEBIT).generateCard(accountEntity);
        cards.add(debitCardEntity);

        if (creditCard) {
            CardEntity creditCardEntity = cardsServiceFactoryService.getCardGenerator(CardTypeEnum.CREDIT).generateCard(accountEntity);
            cards.add(creditCardEntity);
        }
        return cards;
    }

    static String generateBankAccountNumber() {
        // Generates a 9-digit number and adds 100000000 to ensure 10 digits
        return String.format("%10d", RANDOM.nextInt(900000000) + 100000000).trim();
    }
}
