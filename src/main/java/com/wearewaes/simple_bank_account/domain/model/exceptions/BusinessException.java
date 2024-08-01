package com.wearewaes.simple_bank_account.domain.model.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
