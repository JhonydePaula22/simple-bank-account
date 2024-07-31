package com.wearewaes.simple_bank_account.domain.model;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
