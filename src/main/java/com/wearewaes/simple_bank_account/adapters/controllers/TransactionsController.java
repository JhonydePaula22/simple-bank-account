package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.TransactionsApi;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;
import com.wearewaes.simple_bank_account.domain.services.TransactionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TransactionsController extends ControllerAdvice implements TransactionsApi {

    private final TransactionsService transactionsService;

    public TransactionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @Override
    public ResponseEntity<TransactionReceiptDTO> createDepositTransaction(String accountNumber, NewAccountCreditTransactionDTO newAccountCreditTransactionDTO) {
        return ResponseEntity.ok(
                transactionsService.processCreditTransaction(newAccountCreditTransactionDTO, accountNumber, UUID.randomUUID()));
    }

    @Override
    public ResponseEntity<TransactionReceiptDTO> createTransferTransaction(String accountNumber, String destinationAccountNumber, NewAccountDebitTransactionDTO newAccountDebitTransactionDTO) {
        return ResponseEntity.ok(
                transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.TRANSFER, destinationAccountNumber)
        );
    }

    @Override
    public ResponseEntity<TransactionReceiptDTO> createWithdrawTransaction(String accountNumber, NewAccountDebitTransactionDTO newAccountDebitTransactionDTO) {
        return ResponseEntity.ok(
                transactionsService.processDebitTransaction(newAccountDebitTransactionDTO, accountNumber, TransactionTypeEnum.WITHDRAW, null)
        );
    }
}
