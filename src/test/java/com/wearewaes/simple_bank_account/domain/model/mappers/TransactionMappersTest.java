package com.wearewaes.simple_bank_account.domain.model.mappers;

import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionMappersTest {
    @Test
    void testToTransactionReceiptDTO() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionId);
        transactionEntity.setTimestamp(timestamp);

        // Act
        TransactionReceiptDTO transactionReceiptDTO = TransactionMappers.toTransactionReceiptDTO(transactionEntity);

        // Assert
        assertNotNull(transactionReceiptDTO);
        assertEquals(transactionId.toString(), transactionReceiptDTO.getId());
        assertEquals(Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()), transactionReceiptDTO.getTimestamp());
    }

    @Test
    void testGenerateDepositTransaction() {
        // Arrange
        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(100.0);

        AccountEntity accountEntity = new AccountEntity();
        BigDecimal newAccountBalance = BigDecimal.valueOf(200.0);
        UUID transactionReference = UUID.randomUUID();

        // Act
        TransactionEntity transactionEntity = TransactionMappers.generateDepositTransaction(
                newAccountCreditTransactionDTO, accountEntity, newAccountBalance, transactionReference);

        // Assert
        assertNotNull(transactionEntity);
        assertEquals(TransactionTypeEnum.DEPOSIT, transactionEntity.getType());
        assertEquals(BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()), transactionEntity.getAmount());
        assertEquals(BigDecimal.ZERO, transactionEntity.getCreditCardFee());
        assertEquals(BigDecimal.ZERO, transactionEntity.getCreditCardFeeAmount());
        assertEquals(BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()), transactionEntity.getTotalAmount());
        assertEquals(accountEntity, transactionEntity.getAccount());
        assertEquals(newAccountBalance, transactionEntity.getAccountBalance());
        assertEquals(transactionReference, transactionEntity.getRefTransaction());
    }

    @Test
    void testGenerateDebitTransaction() {
        // Arrange
        TransactionTypeEnum transactionType = TransactionTypeEnum.WITHDRAW;
        BigDecimal transactionAmount = BigDecimal.valueOf(100.0);
        BigDecimal transactionFeeAmount = BigDecimal.valueOf(1.0);
        BigDecimal transactionFee = BigDecimal.valueOf(0.01);
        BigDecimal totalTransactionAmount = transactionAmount.add(transactionFeeAmount);

        AccountEntity accountEntity = new AccountEntity();
        BigDecimal newAccountBalance = BigDecimal.valueOf(900.0);
        CardEntity cardEntity = new CardEntity();
        UUID transactionReference = UUID.randomUUID();

        // Act
        TransactionEntity transactionEntity = TransactionMappers.generateDebitTransaction(
                transactionType, transactionAmount, transactionFeeAmount, transactionFee, totalTransactionAmount,
                accountEntity, newAccountBalance, cardEntity, transactionReference);

        // Assert
        assertNotNull(transactionEntity);
        assertEquals(transactionType, transactionEntity.getType());
        assertEquals(transactionAmount, transactionEntity.getAmount());
        assertEquals(transactionFeeAmount, transactionEntity.getCreditCardFeeAmount());
        assertEquals(transactionFee, transactionEntity.getCreditCardFee());
        assertEquals(totalTransactionAmount, transactionEntity.getTotalAmount());
        assertEquals(accountEntity, transactionEntity.getAccount());
        assertEquals(newAccountBalance, transactionEntity.getAccountBalance());
        assertEquals(cardEntity, transactionEntity.getCard());
        assertEquals(transactionReference, transactionEntity.getRefTransaction());
    }

    @Test
    void testGenerateNewAccountCreditTransactionDTO() {
        // Arrange
        NewAccountDebitTransactionDTO newAccountDebitTransactionDTO = new NewAccountDebitTransactionDTO();
        newAccountDebitTransactionDTO.setAmount(100.0);

        // Act
        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = TransactionMappers.generateNewAccountCreditTransactionDTO(newAccountDebitTransactionDTO);

        // Assert
        assertNotNull(newAccountCreditTransactionDTO);
        assertEquals(newAccountDebitTransactionDTO.getAmount(), newAccountCreditTransactionDTO.getAmount());
    }
}