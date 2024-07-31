package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountBalanceDTO;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.CardDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.wearewaes.simple_bank_account.domain.services.AccountsService.generateCard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountsServiceTest {
    private AccountsRepository accountsRepository;
    private AccountHoldersRepository accountHoldersRepository;
    private CardsRepository cardsRepository;
    private AccountsService accountsService;

    @BeforeEach
    void setUp() {
        accountsRepository = Mockito.mock(AccountsRepository.class);
        accountHoldersRepository = Mockito.mock(AccountHoldersRepository.class);
        cardsRepository = Mockito.mock(CardsRepository.class);
        accountsService = new AccountsService(accountsRepository, accountHoldersRepository, cardsRepository);
    }

    @Test
    void testCreateAccount() {
        // Arrange
        AccountHolderDTO accountHolderDTO = generateAccountHolderDto();

        NewAccountDTO newAccountDTO = generateNewAccountDTO(accountHolderDTO, true);

        AccountHolderEntity persistedAccountHolder = generateAccountHolderEntity();

        AccountEntity persistedAccount = generateAccountEntity(persistedAccountHolder, "12345");

        CardEntity debitCardEntity = generateCard(persistedAccount, CardTypeEnum.DEBIT);
        CardEntity creditCardEntity = generateCard(persistedAccount, CardTypeEnum.CREDIT);

        when(accountHoldersRepository.saveAccountHolder(any(AccountHolderEntity.class)))
                .thenReturn(persistedAccountHolder);
        when(accountsRepository.saveAccount(any(AccountEntity.class)))
                .thenReturn(persistedAccount);
        when(cardsRepository.saveCard(any(CardEntity.class)))
                .thenReturn(debitCardEntity)
                .thenReturn(creditCardEntity);

        // Act
        AccountDTO result = accountsService.createAccount(newAccountDTO);

        // Assert
        assertThat(result.getHolder().getId()).isEqualTo("123456");
        assertThat(result.getHolder().getFirstName()).isEqualTo("Jonathan");
        assertThat(result.getHolder().getLastName()).isEqualTo("de Paula");
        assertThat(result.getHolder().getEmail()).isEqualTo("Jonathan.Paula@wearewaes.com");
        assertThat(result.getHolder().getPhone()).isEqualTo("1234567890");
        assertThat(result.getHolder().getAddress()).isEqualTo("Zwaanstraat 31N, 5651 CA Eindhoven");
        assertThat(result.getNumber()).isEqualTo("12345");
        assertThat(result.getBalance()).isEqualTo(0.0);
        assertThat(result.getCards()).hasSize(2);

        CardDTO cardDTO1 = result.getCards().get(0);
        assertThat(cardDTO1.getType()).isEqualTo(CardTypeEnum.DEBIT);

        CardDTO cardDTO2 = result.getCards().get(1);
        assertThat(cardDTO2.getType()).isEqualTo(CardTypeEnum.CREDIT);

        verify(accountHoldersRepository).saveAccountHolder(any(AccountHolderEntity.class));
        verify(accountsRepository).saveAccount(any(AccountEntity.class));
        verify(cardsRepository, times(2)).saveCard(any(CardEntity.class));
    }

    @Test
    void testGenerateCards() {
        // Arrange
        AccountEntity accountEntity = generateAccountEntity(null, "12345");
        boolean creditCard = true;

        CardEntity debitCardEntity = generateCard(accountEntity, CardTypeEnum.DEBIT);
        CardEntity creditCardEntity = generateCard(accountEntity, CardTypeEnum.CREDIT);

        when(cardsRepository.saveCard(any(CardEntity.class)))
                .thenReturn(debitCardEntity)
                .thenReturn(creditCardEntity);

        // Act
        List<CardEntity> result = accountsService.generateCards(accountEntity, creditCard);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(CardTypeEnum.DEBIT);
        assertThat(result.get(1).getType()).isEqualTo(CardTypeEnum.CREDIT);

        verify(cardsRepository, times(2)).saveCard(any(CardEntity.class));
    }

    @Test
    void testGenerateCard() {
        // Arrange
        AccountEntity accountEntity = generateAccountEntity(null, "12345");
        CardTypeEnum cardType = CardTypeEnum.DEBIT;

        // Act
        CardEntity result = generateCard(accountEntity, cardType);

        // Assert
        assertThat(result.getType()).isEqualTo(cardType);
        assertThat(result.getAccount()).isEqualTo(accountEntity);
    }

    @Test
    void testGetAccountSuccess() {
        // Arrange
        String accountNumber = "12345";
        AccountHolderEntity accountHolder = generateAccountHolderEntity();

        AccountEntity accountEntity = generateAccountEntity(accountHolder, accountNumber);
        accountEntity.setNumber(accountNumber);
        List<CardEntity> cards = Collections.emptyList();

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(cardsRepository.findCardsByAccount(accountEntity)).thenReturn(cards);

        // Act
        AccountDTO result = accountsService.getAccount(accountNumber);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isEqualTo(accountNumber);
        verify(accountsRepository).findByNumber(accountNumber);
        verify(cardsRepository).findCardsByAccount(accountEntity);
    }

    @Test
    void testGetAccountNotFound() {
        // Arrange
        String accountNumber = "12345";

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> accountsService.getAccount(accountNumber));
        verify(accountsRepository).findByNumber(accountNumber);
        verify(cardsRepository, never()).findCardsByAccount(any(AccountEntity.class));
    }

    @Test
    void testGetAllAccountsBalance() {
        // Arrange
        Integer offset = 0;
        Integer limit = 10;
        AccountEntity accountEntity1 = generateAccountEntity(generateAccountHolderEntity(), "123345");
        AccountEntity accountEntity2 = generateAccountEntity(generateAccountHolderEntity(), "123366");
        List<AccountEntity> accountEntitiesList = List.of(accountEntity1, accountEntity2);
        Page<AccountEntity> accountEntities = new PageImpl<>(accountEntitiesList, PageRequest.of(offset, limit), accountEntitiesList.size());

        when(accountsRepository.findAll(PageRequest.of(offset, limit))).thenReturn(accountEntities);

        // Act
        AccountsBalanceDTO result = accountsService.getAllAccountsBalance(offset, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccountsBalance()).hasSize(2);

        AccountBalanceDTO accountBalanceDTO1 = result.getAccountsBalance().get(0);
        assertThat(accountBalanceDTO1.getAccountNumber()).isEqualTo("123345");
        assertThat(accountBalanceDTO1.getBalance()).isEqualTo(1000.00);

        AccountBalanceDTO accountBalanceDTO2 = result.getAccountsBalance().get(1);
        assertThat(accountBalanceDTO2.getAccountNumber()).isEqualTo("123366");
        assertThat(accountBalanceDTO2.getBalance()).isEqualTo(2000.00);

        PageDTO pageDTO = result.getPageDetails();
        assertThat(pageDTO).isNotNull();
        assertThat(pageDTO.getFirst()).isTrue();
        assertThat(pageDTO.getLast()).isTrue();
        assertThat(pageDTO.getNumber()).isEqualTo(0);
        assertThat(pageDTO.getTotalPages()).isEqualTo(1);
        assertThat(pageDTO.getTotalElements()).isEqualTo(2);
        assertThat(pageDTO.getSize()).isEqualTo(10);

        verify(accountsRepository, times(1)).findAll(PageRequest.of(offset, limit));
    }

    private static NewAccountDTO generateNewAccountDTO(AccountHolderDTO accountHolderDTO, boolean creditCard) {
        NewAccountDTO newAccountDTO = new NewAccountDTO();
        newAccountDTO.setHolder(accountHolderDTO);
        newAccountDTO.setCreditCard(creditCard);
        return newAccountDTO;
    }

    private static AccountHolderDTO generateAccountHolderDto() {
        AccountHolderDTO accountHolderDTO = new AccountHolderDTO();
        accountHolderDTO.setId("123456");
        accountHolderDTO.setFirstName("Jonathan");
        accountHolderDTO.setLastName("de Paula");
        accountHolderDTO.setEmail("Jonathan.Paula@wearewaes.com");
        accountHolderDTO.setPhone("1234567890");
        accountHolderDTO.setAddress("Zwaanstraat 31N, 5651 CA Eindhoven");
        return accountHolderDTO;
    }

    private static AccountEntity generateAccountEntity(AccountHolderEntity persistedAccountHolder, String number) {
        return new AccountEntity(
                UUID.randomUUID(), persistedAccountHolder, number, BigDecimal.ZERO);
    }

    private static AccountHolderEntity generateAccountHolderEntity() {
        return new AccountHolderEntity(
                UUID.randomUUID(),
                "123456",
                "Jonathan",
                "de Paula",
                "Jonathan.Paula@wearewaes.com",
                "1234567890",
                "Zwaanstraat 31N, 5651 CA Eindhoven");
    }
}