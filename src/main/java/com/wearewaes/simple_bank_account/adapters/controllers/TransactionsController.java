package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.TransactionsApi;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.commands.TransactionsCommands;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionsController implements TransactionsApi {

    private final TransactionsCommands transactionsCommands;

    public TransactionsController(TransactionsCommands transactionsCommands) {
        this.transactionsCommands = transactionsCommands;
    }

    @Override
    public ResponseEntity<TransactionReceiptDTO> createDepositTransaction(String accountNumber, NewAccountCreditTransactionDTO newAccountCreditTransactionDTO) {
        return ResponseEntity.ok(
                transactionsCommands.processDepositTransaction(newAccountCreditTransactionDTO, accountNumber));
    }

    @Override
    public ResponseEntity<TransactionReceiptDTO> createTransferTransaction(String accountNumber, String destinationAccountNumber, NewAccountDebitTransactionDTO newAccountDebitTransactionDTO) {
        return ResponseEntity.internalServerError().build();
    }

    @Override
    public ResponseEntity<TransactionReceiptDTO> createWithdrawTransaction(String accountNumber, NewAccountDebitTransactionDTO newAccountDebitTransactionDTO) {
        return ResponseEntity.internalServerError().build();
    }
}
