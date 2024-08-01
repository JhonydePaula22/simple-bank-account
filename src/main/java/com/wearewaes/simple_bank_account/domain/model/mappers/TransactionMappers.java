package com.wearewaes.simple_bank_account.domain.model.mappers;

import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class TransactionMappers {

    public static TransactionReceiptDTO toTransactionReceiptDTO(TransactionEntity transactionEntity) {
        TransactionReceiptDTO transactionReceiptDTO = new TransactionReceiptDTO();
        transactionReceiptDTO.setId(transactionEntity.getId().toString());
        transactionReceiptDTO.setTimestamp(
                Date.from(transactionEntity.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        return transactionReceiptDTO;
    }



    public static TransactionEntity generateDepositTransaction(NewAccountCreditTransactionDTO newAccountCreditTransactionDTO,
                                                                AccountEntity accountEntity,
                                                                BigDecimal newAccountBalance,
                                                                UUID transactionReference) {
        return new TransactionEntity(
                null,
                LocalDateTime.now(),
                TransactionTypeEnum.DEPOSIT,
                BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()),
                accountEntity,
                newAccountBalance,
                null,
                transactionReference);
    }

    public static TransactionEntity generateDebitTransaction(TransactionTypeEnum transactionType,
                                                              BigDecimal transactionAmount,
                                                              BigDecimal transactionFeeAmount,
                                                              BigDecimal transactionFee,
                                                              BigDecimal totalTransactionAmount,
                                                              AccountEntity accountEntity,
                                                              BigDecimal newAccountBalance,
                                                              CardEntity cardEntity,
                                                              UUID transactionReference) {
        return new TransactionEntity(
                null,
                LocalDateTime.now(),
                transactionType,
                transactionAmount,
                transactionFeeAmount,
                transactionFee,
                totalTransactionAmount,
                accountEntity,
                newAccountBalance,
                cardEntity,
                transactionReference
        );
    }
}
