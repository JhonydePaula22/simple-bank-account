package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BadRequestException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BusinessException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @Mock
    private CardsRepository cardsRepository;

    private EncryptionService encryptionService;

    @InjectMocks
    private TransactionsService transactionsService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService("5lyi1fhGSeoBrI0+qERnWBUJmitWJ9IX3GVCYqANmt4=");
        transactionsService = new TransactionsService(transactionsRepository, accountsRepository,
                cardsRepository, encryptionService);
    }

    @Test
    void testProcessCreditTransaction() {
        // Arrange
        String accountNumber = "123456";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(1000));

        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(500.00);

        UUID transactionRef = UUID.randomUUID();

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
                transactionRef);

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(transactionsRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);

        // Act
        TransactionReceiptDTO receiptDTO = transactionsService.processCreditTransaction(newAccountCreditTransactionDTO, accountNumber, transactionRef);

        // Assert
        assertNotNull(receiptDTO);
        assertNotNull(receiptDTO.getId());
        assertNotNull(receiptDTO.getTimestamp());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(transactionsRepository, times(1)).save(any(TransactionEntity.class));
        verify(accountsRepository, times(1)).saveAccount(accountEntity);
    }

    @Test
    void testProcessCreditTransactionWithInvalidAccountNumber() {
        // Arrange
        String invalidAccountNumber = "999999";
        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(500.00);

        when(accountsRepository.findByNumber(invalidAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                transactionsService.processCreditTransaction(newAccountCreditTransactionDTO, invalidAccountNumber, UUID.randomUUID()));
        assertEquals("The account number informed is not a valid one.", exception.getMessage());

        verify(accountsRepository, times(1)).findByNumber(invalidAccountNumber);
        verify(transactionsRepository, never()).save(any(TransactionEntity.class));
        verify(accountsRepository, never()).saveAccount(any(AccountEntity.class));
    }

    @Test
    void testProcessDebitTransaction() {
        // Arrange
        String accountNumber = "123456";
        String destinationAccountNumber = "654321";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(1000));

        CardDTO cardDTO = new CardDTO();
        cardDTO.setType(CardTypeEnum.DEBIT);
        cardDTO.setNumber("1234");
        cardDTO.setSecurityCode("000");

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber(encryptionService.encrypt("1234"));
        cardEntity.setCvv(encryptionService.encrypt("000"));
        cardEntity.setType(CardTypeEnum.DEBIT);

        NewAccountDebitTransactionDTO newAccountDebitTransactionDTO = new NewAccountDebitTransactionDTO();
        newAccountDebitTransactionDTO.setAmount(500.00);
        newAccountDebitTransactionDTO.setCard(cardDTO);

        TransactionEntity transactionEntity = new TransactionEntity(
                UUID.randomUUID(),
                LocalDateTime.now(),
                TransactionTypeEnum.DEPOSIT,
                BigDecimal.valueOf(newAccountDebitTransactionDTO.getAmount()),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(newAccountDebitTransactionDTO.getAmount()),
                accountEntity,
                accountEntity.getBalance().subtract(BigDecimal.valueOf(newAccountDebitTransactionDTO.getAmount())),
                cardEntity,
                UUID.randomUUID());

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(cardsRepository.findCardsByAccount(accountEntity)).thenReturn(List.of(cardEntity));
        when(transactionsRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);

        // Act
        TransactionReceiptDTO receiptDTO = transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.DEPOSIT, destinationAccountNumber);

        // Assert
        assertNotNull(receiptDTO);
        assertNotNull(receiptDTO.getTimestamp());
        assertNotNull(receiptDTO.getId());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(transactionsRepository, times(1)).save(any(TransactionEntity.class));
        verify(accountsRepository, times(1)).saveAccount(accountEntity);
        verify(cardsRepository, times(1)).findCardsByAccount(accountEntity);
    }

    @Test
    void testProcessDebitTransactionWithCreditCard() {
        // Arrange
        String accountNumber = "123456";
        String destinationAccountNumber = "654321";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(1000));

        AccountEntity accountEntity2 = new AccountEntity();
        accountEntity2.setNumber(destinationAccountNumber);
        accountEntity2.setBalance(BigDecimal.valueOf(100));

        CardDTO cardDTO = new CardDTO();
        cardDTO.setType(CardTypeEnum.CREDIT);
        cardDTO.setNumber("1234");
        cardDTO.setSecurityCode("000");

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber(encryptionService.encrypt("1234"));
        cardEntity.setCvv(encryptionService.encrypt("000"));
        cardEntity.setType(CardTypeEnum.CREDIT);

        NewAccountDebitTransactionDTO newAccountDebitTransactionDTO = new NewAccountDebitTransactionDTO();
        newAccountDebitTransactionDTO.setAmount(500.00);
        newAccountDebitTransactionDTO.setCard(cardDTO);

        BigDecimal transactionAmount = BigDecimal.valueOf(newAccountDebitTransactionDTO.getAmount());
        BigDecimal transactionFeeAmount = transactionAmount.multiply(BigDecimal.valueOf(0.01));
        BigDecimal totalTransactionAmount = transactionAmount.add(transactionFeeAmount);

        TransactionEntity transactionEntity = new TransactionEntity(
                UUID.randomUUID(),
                LocalDateTime.now(),
                TransactionTypeEnum.TRANSFER,
                transactionAmount,
                transactionFeeAmount,
                BigDecimal.valueOf(0.01),
                totalTransactionAmount,
                accountEntity,
                accountEntity.getBalance().subtract(totalTransactionAmount),
                cardEntity,
                UUID.randomUUID());

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(accountsRepository.findByNumber(destinationAccountNumber)).thenReturn(Optional.of(accountEntity2));
        when(cardsRepository.findCardsByAccount(accountEntity)).thenReturn(List.of(cardEntity));
        when(transactionsRepository.save(any(TransactionEntity.class))).thenReturn(transactionEntity);

        // Act
        TransactionReceiptDTO receiptDTO = transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.TRANSFER, destinationAccountNumber);

        // Assert
        assertNotNull(receiptDTO);
        assertNotNull(receiptDTO.getTimestamp());
        assertNotNull(receiptDTO.getId());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(accountsRepository, times(1)).findByNumber(destinationAccountNumber);
        verify(transactionsRepository, times(2)).save(any(TransactionEntity.class));
        verify(accountsRepository, times(1)).saveAccount(accountEntity);
        verify(cardsRepository, times(1)).findCardsByAccount(accountEntity);
    }

    @Test
    void testProcessDebitTransactionWithInsufficientFunds() {
        // Arrange
        String accountNumber = "123456";
        String destinationAccountNumber = "654321";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(100));

        CardDTO cardDTO = new CardDTO();
        cardDTO.setType(CardTypeEnum.CREDIT);
        cardDTO.setNumber("1234");
        cardDTO.setSecurityCode("000");

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber(encryptionService.encrypt("1234"));
        cardEntity.setCvv(encryptionService.encrypt("000"));
        cardEntity.setType(CardTypeEnum.CREDIT);

        NewAccountDebitTransactionDTO newAccountDebitTransactionDTO = new NewAccountDebitTransactionDTO();
        newAccountDebitTransactionDTO.setAmount(500.00);
        newAccountDebitTransactionDTO.setCard(cardDTO);

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(cardsRepository.findCardsByAccount(accountEntity)).thenReturn(List.of(cardEntity));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
                transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.TRANSFER, destinationAccountNumber));
        assertEquals("Your account does not have enough funds to complete this operation.", exception.getMessage());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(cardsRepository, times(1)).findCardsByAccount(accountEntity);
        verify(transactionsRepository, never()).save(any(TransactionEntity.class));
        verify(accountsRepository, never()).saveAccount(any(AccountEntity.class));
    }

    @Test
    void testProcessDebitTransactionWithWrongCardNumber() {
        // Arrange
        String accountNumber = "123456";
        String destinationAccountNumber = "654321";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(100));

        CardDTO cardDTO = new CardDTO();
        cardDTO.setType(CardTypeEnum.CREDIT);
        cardDTO.setNumber(encryptionService.encrypt("1234"));
        cardDTO.setSecurityCode(encryptionService.encrypt("000"));

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber(encryptionService.encrypt("1233"));
        cardEntity.setCvv(encryptionService.encrypt("000"));
        cardEntity.setType(CardTypeEnum.CREDIT);

        NewAccountDebitTransactionDTO newAccountDebitTransactionDTO = new NewAccountDebitTransactionDTO();
        newAccountDebitTransactionDTO.setAmount(500.00);
        newAccountDebitTransactionDTO.setCard(cardDTO);

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(cardsRepository.findCardsByAccount(accountEntity)).thenReturn(List.of(cardEntity));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.TRANSFER, destinationAccountNumber));
        assertEquals("Card data is invalid. Please check and try again.", exception.getMessage());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(cardsRepository, times(1)).findCardsByAccount(accountEntity);
        verify(transactionsRepository, never()).save(any(TransactionEntity.class));
        verify(accountsRepository, never()).saveAccount(any(AccountEntity.class));
    }

    @Test
    void testProcessDebitTransactionWithWrongCardCvvNumber() {
        // Arrange
        String accountNumber = "123456";
        String destinationAccountNumber = "654321";
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setNumber(accountNumber);
        accountEntity.setBalance(BigDecimal.valueOf(100));

        CardDTO cardDTO = new CardDTO();
        cardDTO.setType(CardTypeEnum.CREDIT);
        cardDTO.setNumber(encryptionService.encrypt("1234"));
        cardDTO.setSecurityCode(encryptionService.encrypt("000"));

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber(encryptionService.encrypt("1234"));
        cardEntity.setCvv(encryptionService.encrypt("001"));
        cardEntity.setType(CardTypeEnum.CREDIT);

        NewAccountDebitTransactionDTO newAccountDebitTransactionDTO = new NewAccountDebitTransactionDTO();
        newAccountDebitTransactionDTO.setAmount(500.00);
        newAccountDebitTransactionDTO.setCard(cardDTO);

        when(accountsRepository.findByNumber(accountNumber)).thenReturn(Optional.of(accountEntity));
        when(cardsRepository.findCardsByAccount(accountEntity)).thenReturn(List.of(cardEntity));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.TRANSFER, destinationAccountNumber));
        assertEquals("Card data is invalid. Please check and try again.", exception.getMessage());

        verify(accountsRepository, times(1)).findByNumber(accountNumber);
        verify(cardsRepository, times(1)).findCardsByAccount(accountEntity);
        verify(transactionsRepository, never()).save(any(TransactionEntity.class));
        verify(accountsRepository, never()).saveAccount(any(AccountEntity.class));
    }
}