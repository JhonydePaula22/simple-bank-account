package com.wearewaes.simple_bank_account;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.CardDTO;
import com.wearewaes.model.NewAccountDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.wearewaes.model.CardTypeEnum.CREDIT;
import static com.wearewaes.model.CardTypeEnum.DEBIT;

public class TestUtils {

    public static AccountDTO generateAccount(NewAccountDTO newAccount) {
        AccountDTO account = new AccountDTO();
        account.setNumber("123456");

        CardDTO debitCard = new CardDTO();
        debitCard.setNumber("758497584907890");
        debitCard.setSecurityCode("123");
        debitCard.setType(DEBIT);

        List<CardDTO> cards = new ArrayList<>();
        cards.add(debitCard);

        if (newAccount.getCreditCard()) {
            CardDTO creditCard = new CardDTO();
            creditCard.setNumber("758497584907891");
            creditCard.setSecurityCode("321");
            creditCard.setType(CREDIT);
            cards.add(creditCard);
        }
        account.setCards(cards);
        account.setHolder(newAccount.getHolder());
        return account;
    }

    public static NewAccountDTO generateNewAccount(boolean creditCard, String identification) {
        NewAccountDTO newAccount = new NewAccountDTO();
        AccountHolderDTO accountHolder = new AccountHolderDTO();
        accountHolder.setId(identification);
        accountHolder.setFirstName("Jonathan");
        accountHolder.setLastName("de Paula");
        accountHolder.setEmail("jonathan.paula"+ LocalDateTime.now() +"@wearewaes.com");
        accountHolder.setPhone("06200000000");
        accountHolder.setAddress("Zwaanstraat 31N, 5651 CA Eindhoven");
        newAccount.setHolder(accountHolder);
        newAccount.setCreditCard(creditCard);
        return newAccount;
    }
}
