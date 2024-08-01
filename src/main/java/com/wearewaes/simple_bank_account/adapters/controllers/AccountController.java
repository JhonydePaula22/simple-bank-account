package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.AccountsApi;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.commands.AccountCommands;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BusinessException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.InternalErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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
        return ResponseEntity.ok(accountCommands.getAllAccountsBalance(offset, limit));
    }

    @ExceptionHandler(value = AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEAccountNotFoundExceptions(AccountNotFoundException exception) {
        ProblemDetail problemDetail = generateProblemDetail(exception, BAD_REQUEST, "Account not found");
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(value = {BusinessException.class})
    public ResponseEntity<ProblemDetail> handleBusinessExceptions(Exception exception) {
        ProblemDetail problemDetail = generateProblemDetail(exception, BAD_REQUEST, "Invalid request");
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(value = InternalErrorException.class)
    public ResponseEntity<ProblemDetail> handleBusinessExceptions(InternalErrorException exception) {
        ProblemDetail problemDetail =
                generateProblemDetail(exception, INTERNAL_SERVER_ERROR, "Internal server error");
        return ResponseEntity.badRequest().body(problemDetail);
    }

    private static ProblemDetail generateProblemDetail(Exception exception, HttpStatus httpStatus, String title) {
        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, exception.getMessage());
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
