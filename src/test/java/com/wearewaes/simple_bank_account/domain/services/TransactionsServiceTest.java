package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTest {

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private AccountsRepository accountsRepository;

    @InjectMocks
    private TransactionsService transactionsService;

    @BeforeEach
    void setUp() {
        transactionsService = new TransactionsService(transactionsRepository, accountsRepository);
    }

    @Test
    void testProcessDepositTransaction() {
        // Arrange
        String accountNumber = "123456";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(1000));

        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(500.00);

        TransactionEntity transactionEntity = new TransactionEntity(
                UUID.randomUUID(),
                LocalDateTime.now(),
                TransactionTypeEnum.DEPOSIT,
                BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()),
                accountEntity,
                accountEntity.getBalance().add(BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount())),
                null,
                UUID.randomUUID());

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(transactionsRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);

        // Act
        TransactionReceiptDTO receiptDTO = transactionsService.processDepositTransaction(newAccountCreditTransactionDTO, accountNumber);

        // Assert
        assertNotNull(receiptDTO);
        assertNotNull(receiptDTO.getId());
        assertNotNull(receiptDTO.getTimestamp());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(transactionsRepository, times(1)).save(any(TransactionEntity.class));
        verify(accountsRepository, times(1)).saveAccount(accountEntity);
    }

    @Test
    void testProcessDepositTransactionWithInvalidAccountNumber() {
        // Arrange
        String invalidAccountNumber = "999999";
        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(500.00);

        when(accountsRepository.findByNumber(invalidAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                transactionsService.processDepositTransaction(newAccountCreditTransactionDTO, invalidAccountNumber));
        assertEquals("The account number informed is not a valid one.", exception.getMessage());

        verify(accountsRepository, times(1)).findByNumber(invalidAccountNumber);
        verify(transactionsRepository, never()).save(any(TransactionEntity.class));
        verify(accountsRepository, never()).saveAccount(any(AccountEntity.class));
    }
}