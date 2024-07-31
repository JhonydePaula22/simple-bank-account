package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.CardDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        AccountHolderDTO accountHolderDTO = new AccountHolderDTO();
        accountHolderDTO.setId("123456");
        accountHolderDTO.setFirstName("Jonathan");
        accountHolderDTO.setLastName("de Paula");
        accountHolderDTO.setEmail("Jonathan.Paula@wearewaes.com");
        accountHolderDTO.setPhone("1234567890");
        accountHolderDTO.setAddress("Zwaanstraat 31N, 5651 CA Eindhoven");

        NewAccountDTO newAccountDTO = new NewAccountDTO();
        newAccountDTO.setHolder(accountHolderDTO);
        newAccountDTO.setCreditCard(true);

        AccountHolderEntity persistedAccountHolder = new AccountHolderEntity(
                UUID.randomUUID(), "123456", "Jonathan", "de Paula", "Jonathan.Paula@wearewaes.com", "1234567890", "Zwaanstraat 31N, 5651 CA Eindhoven");

        AccountEntity persistedAccount = new AccountEntity(
                UUID.randomUUID(), persistedAccountHolder, "12345", BigDecimal.ZERO);

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
        AccountEntity accountEntity = new AccountEntity(
                UUID.randomUUID(), null, "12345", BigDecimal.ZERO);
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
        AccountEntity accountEntity = new AccountEntity(
                UUID.randomUUID(), null, "12345", BigDecimal.ZERO);
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
        AccountHolderEntity accountHolder = new AccountHolderEntity(
                UUID.randomUUID(), "123456", "Jonathan", "de Paula", "Jonathan.Paula@wearewaes.com", "1234567890", "Zwaanstraat 31N, 5651 CA Eindhoven");

        AccountEntity accountEntity = new AccountEntity(
                UUID.randomUUID(), accountHolder, "12345", BigDecimal.ZERO);
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
}