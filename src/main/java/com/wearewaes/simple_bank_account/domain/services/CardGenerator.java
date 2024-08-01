package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;

import static com.wearewaes.simple_bank_account.domain.services.AccountsService.RANDOM;

public interface CardGenerator {

    CardEntity generateCard(AccountEntity accountEntity);

    default String generateCardNumber() {
        return String.format("%14d", RANDOM.nextLong() & 0xFFFFFFFFFFFFL).trim(); // 48-bit card number
    }

    default String generateCVV() {
        // Generate a number with cvvLength digits
        int cvv = RANDOM.nextInt((int) Math.pow(10, 3));
        // Format the CVV to ensure it has the correct number of digits
        return String.format("%03d", cvv).trim();
    }
}
