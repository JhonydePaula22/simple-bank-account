package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountBalanceDTO;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.model.PageDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public AccountDTO getAccount(String accountNumber) {
        AccountEntity accountEntity = accountsRepository.findByNumber(accountNumber).orElseThrow(
                () -> new AccountNotFoundException("The account number informed is not a valid one."));
        List<CardEntity> cards = cardsRepository.findCardsByAccount(accountEntity);
        return toDtoMapper(accountEntity, cards);
    }

    public AccountsBalanceDTO getAllAccountsBalance(Integer offset, Integer limit) {
        Page<AccountEntity> accountEntities = accountsRepository.findAll(PageRequest.of(offset, limit));
        PageDTO pageDTO = populatePageDTOData(accountEntities);
        AccountsBalanceDTO accountsBalanceDTO = new AccountsBalanceDTO();
        accountsBalanceDTO.setPageDetails(pageDTO);
        accountsBalanceDTO.setAccountsBalance(accountEntities.map(accountEntity -> {
            AccountBalanceDTO accountBalanceDTO = new AccountBalanceDTO();
            accountBalanceDTO.setAccountNumber(accountEntity.getNumber());
            accountBalanceDTO.setBalance(accountEntity.getBalance().doubleValue());
            return accountBalanceDTO;
        }).stream().toList());
        return accountsBalanceDTO;
    }

    private static PageDTO populatePageDTOData(Page<AccountEntity> accountEntities) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setFirst(accountEntities.isFirst());
        pageDTO.setLast(accountEntities.isLast());
        pageDTO.setNumber(accountEntities.getNumber());
        pageDTO.setTotalPages(accountEntities.getTotalPages());
        pageDTO.setTotalElements(accountEntities.getTotalElements());
        pageDTO.setSize(accountEntities.getSize());
        return pageDTO;
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
        return String.format("%14d", RANDOM.nextLong() & 0xFFFFFFFFFFFFL).trim(); // 48-bit card number
    }

    static String generateCVV() {
        // Generate a number with cvvLength digits
        int cvv = RANDOM.nextInt((int) Math.pow(10, 3));
        // Format the CVV to ensure it has the correct number of digits
        return String.format("%03d", cvv).trim();
    }

    static String generateBankAccountNumber() {
        // Generates a 9-digit number and adds 100000000 to ensure 10 digits
        return String.format("%10d", RANDOM.nextInt(900000000) + 100000000).trim();
    }
}
