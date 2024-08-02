package com.wearewaes.simple_bank_account.adapters.controllers;


import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BadRequestException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BusinessException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.InternalErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ControllerAdvice {

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

    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(BadRequestException exception) {
        ProblemDetail problemDetail =
                generateProblemDetail(exception, BAD_REQUEST, "Invalid data on the request");
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ProblemDetail> handleBadRequestException(Exception exception) {
        if (exception.getMessage().contains("could not serialize access due to concurrent update")) {
            ProblemDetail problemDetail =
                    generateProblemDetail(new InternalErrorException("We could not process your transaction. Try again!"), INTERNAL_SERVER_ERROR, "Internal Server error");
            return ResponseEntity.badRequest().body(problemDetail);
        }
        ProblemDetail problemDetail =
                generateProblemDetail(exception, BAD_REQUEST, "Invalid data on the request");
        return ResponseEntity.badRequest().body(problemDetail);
    }

    private static ProblemDetail generateProblemDetail(Exception exception, HttpStatus httpStatus, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, exception.getMessage());
        problemDetail.setTitle(title);
        return problemDetail;
    }
}
