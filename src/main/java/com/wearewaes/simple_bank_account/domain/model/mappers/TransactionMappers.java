package com.wearewaes.simple_bank_account.domain.model.mappers;

import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;

import java.time.ZoneId;
import java.util.Date;

public class TransactionMappers {

    public static TransactionReceiptDTO toTransactionReceiptDTO(TransactionEntity transactionEntity) {
        TransactionReceiptDTO transactionReceiptDTO = new TransactionReceiptDTO();
        transactionReceiptDTO.setId(transactionEntity.getId().toString());
        transactionReceiptDTO.setTimestamp(
                Date.from(transactionEntity.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()));
        return transactionReceiptDTO;
    }
}
