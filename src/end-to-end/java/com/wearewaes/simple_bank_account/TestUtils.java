package com.wearewaes.simple_bank_account;

import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.NewAccountDTO;

import java.time.LocalDateTime;
import java.util.Random;

public class TestUtils {

    private final static Random RANDOM = new Random();

    public static NewAccountDTO generateNewAccount(boolean creditCard) {
        NewAccountDTO newAccount = new NewAccountDTO();
        AccountHolderDTO accountHolder = new AccountHolderDTO();
        accountHolder.setId(LocalDateTime.now() + "_" + RANDOM.nextInt(0, 1000));
        accountHolder.setFirstName("Jonathan");
        accountHolder.setLastName("de Paula");
        accountHolder.setEmail("jonathan.paula"+ LocalDateTime.now() + "_" + RANDOM.nextInt(0, 1000) +"@wearewaes.com");
        accountHolder.setPhone("06200000000");
        accountHolder.setAddress("Zwaanstraat 31N, 5651 CA Eindhoven");
        newAccount.setHolder(accountHolder);
        newAccount.setCreditCard(creditCard);
        return newAccount;
    }
}
