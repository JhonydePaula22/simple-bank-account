package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.AccountsApi;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.commands.AccountCommands;
import com.wearewaes.simple_bank_account.domain.model.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
public class AccountController implements AccountsApi {

    private final AccountCommands accountCommands;

    public AccountController(AccountCommands accountCommands) {
        this.accountCommands = accountCommands;
    }

    @Override
    public ResponseEntity<AccountDTO> createAccount(NewAccountDTO newAccountDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountCommands.createAccount(newAccountDTO));
    }

    @Override
    public ResponseEntity<AccountDTO> getAccount(String accountNumber) {
        return ResponseEntity.ok(accountCommands.getAccount(accountNumber));
    }

    @Override
    public ResponseEntity<AccountsBalanceDTO> getAllAccountsBalance(Integer offset, Integer limit) {
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(value = AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEdiTrainExceptions(AccountNotFoundException exception) {
        return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(BAD_REQUEST, exception.getMessage()));
    }
}
