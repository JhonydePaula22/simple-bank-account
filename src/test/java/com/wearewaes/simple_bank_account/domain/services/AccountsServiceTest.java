package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.CardDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountsServiceTest {
    private AccountsRepository accountsRepository;
    private AccountHoldersRepository accountHoldersRepository;
    private CardsServiceFactory cardsServiceFactory;
    private AccountsService accountsService;
    private CreateDebitCardService createDebitCardService;
    private CreateCreditCardService createCreditCardService;

    @BeforeEach
    void setUp() {
        accountsRepository = Mockito.mock(AccountsRepository.class);
        accountHoldersRepository = Mockito.mock(AccountHoldersRepository.class);
        cardsServiceFactory = Mockito.mock(CardsServiceFactory.class);
        createDebitCardService = Mockito.mock(CreateDebitCardService.class);
        createCreditCardService = Mockito.mock(CreateCreditCardService.class);
        accountsService = new AccountsService(accountsRepository, accountHoldersRepository, cardsServiceFactory);
    }

    @Test
    void testCreateAccount() {
        // Arrange
        AccountHolderDTO accountHolderDTO = generateAccountHolderDto();

        NewAccountDTO newAccountDTO = generateNewAccountDTO(accountHolderDTO, true);

        AccountHolderEntity persistedAccountHolder = generateAccountHolderEntity();

        AccountEntity persistedAccount = generateAccountEntity(persistedAccountHolder, "12345");

        CardEntity debitCardEntity = new CardEntity(
                "7843527158943",
                "123",
                CardTypeEnum.DEBIT,
                persistedAccount
        );
        CardEntity creditCardEntity = new CardEntity(
                "7843527158944",
                "124",
                CardTypeEnum.CREDIT,
                persistedAccount
        );

        when(accountHoldersRepository.saveAccountHolder(any(AccountHolderEntity.class)))
                .thenReturn(persistedAccountHolder);
        when(accountsRepository.saveAccount(any(AccountEntity.class)))
                .thenReturn(persistedAccount);
        when(cardsServiceFactory.getCardGenerator(CardTypeEnum.CREDIT))
                .thenReturn(createCreditCardService);
        when(cardsServiceFactory.getCardGenerator(CardTypeEnum.DEBIT))
                .thenReturn(createDebitCardService);

        when(createCreditCardService.generateCard(persistedAccount)).thenReturn(creditCardEntity);
        when(createDebitCardService.generateCard(persistedAccount)).thenReturn(debitCardEntity);

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
        verify(cardsServiceFactory, times(2)).getCardGenerator(any(CardTypeEnum.class));
        verify(createCreditCardService, times(1)).generateCard(any(AccountEntity.class));
        verify(createDebitCardService, times(1)).generateCard(any(AccountEntity.class));
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