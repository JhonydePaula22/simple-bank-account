package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountBalanceDTO;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.PageDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAccountServiceTest {
    private AccountsRepository accountsRepository;
    private AccountHoldersRepository accountHoldersRepository;
    private CardsRepository cardsRepository;
    private GetAccountService getAccountService;

    @BeforeEach
    void setUp() {
        accountsRepository = Mockito.mock(AccountsRepository.class);
        cardsRepository = Mockito.mock(CardsRepository.class);
        getAccountService = new GetAccountService(accountsRepository, cardsRepository);
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
        AccountDTO result = getAccountService.getAccount(accountNumber);

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
        assertThrows(AccountNotFoundException.class, () -> getAccountService.getAccount(accountNumber));
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
        AccountsBalanceDTO result = getAccountService.getAllAccountsBalance(offset, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccountsBalance()).hasSize(2);

        AccountBalanceDTO accountBalanceDTO1 = result.getAccountsBalance().get(0);
        assertThat(accountBalanceDTO1.getAccountNumber()).isEqualTo("123345");
        assertThat(accountBalanceDTO1.getBalance()).isEqualTo(0.00);

        AccountBalanceDTO accountBalanceDTO2 = result.getAccountsBalance().get(1);
        assertThat(accountBalanceDTO2.getAccountNumber()).isEqualTo("123366");
        assertThat(accountBalanceDTO2.getBalance()).isEqualTo(0.00);

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