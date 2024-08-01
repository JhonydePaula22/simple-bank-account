package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import jakarta.persistence.PersistenceException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.wearewaes.simple_bank_account.domain.model.mappers.TransactionMappers.toTransactionReceiptDTO;

public class TransactionsService {

    private final TransactionsRepository transactionsRepository;
    private final AccountsRepository accountsRepository;

    public TransactionsService(TransactionsRepository transactionsRepository, AccountsRepository accountsRepository) {
        this.transactionsRepository = transactionsRepository;
        this.accountsRepository = accountsRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = PersistenceException.class)
    public TransactionReceiptDTO processDepositTransaction(
            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO, String accountNumber) {
        AccountEntity accountEntity = getAccountEntity(accountNumber);
        TransactionEntity transactionEntity = generateTransactionEntity(newAccountCreditTransactionDTO, accountEntity);
        saveAccountEntityChanges(accountEntity);
        var persistedTransactionEntity = transactionsRepository.save(transactionEntity);
        return toTransactionReceiptDTO(persistedTransactionEntity);
    }

    private static TransactionEntity generateTransactionEntity(NewAccountCreditTransactionDTO newAccountCreditTransactionDTO, AccountEntity accountEntity) {
        var currentAccountBalance = accountEntity.getBalance();
        var newAccountBalance = currentAccountBalance.add(BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()));
        TransactionEntity transactionEntity = generateTransaction(newAccountCreditTransactionDTO, accountEntity, newAccountBalance);
        accountEntity.setBalance(newAccountBalance);
        return transactionEntity;
    }

    private static TransactionEntity generateTransaction(NewAccountCreditTransactionDTO newAccountCreditTransactionDTO,
                                                         AccountEntity accountEntity, 
                                                         BigDecimal newAccountBalance) {
        TransactionEntity transactionEntity = new TransactionEntity(
                null,
                LocalDateTime.now(),
                TransactionTypeEnum.DEPOSIT,
                BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount()),
                accountEntity,
                newAccountBalance,
                null,
                UUID.randomUUID());
        return transactionEntity;
    }

    public AccountEntity getAccountEntity(String accountNumber) {
        return accountsRepository.findByNumber(accountNumber).orElseThrow(
                () -> new AccountNotFoundException("The account number informed is not a valid one."));
    }

    public void saveAccountEntityChanges(AccountEntity accountEntity) {
        accountsRepository.saveAccount(accountEntity);
    }
}
