package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardDTO;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionEntity;
import com.wearewaes.simple_bank_account.domain.model.TransactionTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BadRequestException;
import com.wearewaes.simple_bank_account.domain.model.exceptions.BusinessException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.wearewaes.simple_bank_account.domain.model.mappers.TransactionMappers.generateDebitTransaction;
import static com.wearewaes.simple_bank_account.domain.model.mappers.TransactionMappers.generateDepositTransaction;
import static com.wearewaes.simple_bank_account.domain.model.mappers.TransactionMappers.generateNewAccountCreditTransactionDTO;
import static com.wearewaes.simple_bank_account.domain.model.mappers.TransactionMappers.toTransactionReceiptDTO;

@Slf4j
public class TransactionsService {

    public static final int ANY_NEGATIVE_NUMBER = -1;
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    private final TransactionsRepository transactionsRepository;
    private final AccountsRepository accountsRepository;
    private final CardsRepository cardsRepository;
    private final EncryptionService encryptionService;
    private final CardsFeeService cardsFeeService;

    public TransactionsService(TransactionsRepository transactionsRepository,
                               AccountsRepository accountsRepository,
                               CardsRepository cardsRepository,
                               EncryptionService encryptionService,
                               CardsFeeService cardsFeeService) {
        this.transactionsRepository = transactionsRepository;
        this.accountsRepository = accountsRepository;
        this.cardsRepository = cardsRepository;
        this.encryptionService = encryptionService;
        this.cardsFeeService = cardsFeeService;
    }

    // will prevent any delete or update on the entities in the session to happen.
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = RuntimeException.class)
    public TransactionReceiptDTO processCreditTransaction(
            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO, String accountNumber,
            UUID transactionReference) {
        AccountEntity accountEntity = getAccountEntity(accountNumber);
        BigDecimal currentAccountBalance = accountEntity.getBalance();
        BigDecimal transactionAmount = BigDecimal.valueOf(newAccountCreditTransactionDTO.getAmount());
        BigDecimal newAccountBalance = currentAccountBalance.add(transactionAmount);
        TransactionEntity transactionEntity = generateDepositTransaction(
                newAccountCreditTransactionDTO,
                accountEntity,
                newAccountBalance,
                transactionReference);
        accountEntity.setBalance(newAccountBalance);
        saveAccountEntityChanges(accountEntity);
        TransactionEntity persistedTransactionEntity = transactionsRepository.save(transactionEntity);
        logTransaction(transactionEntity);
        return toTransactionReceiptDTO(persistedTransactionEntity);
    }

    // will prevent any delete or update on the entities in the session to happen.
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = RuntimeException.class)
    public TransactionReceiptDTO processDebitTransaction(NewAccountDebitTransactionDTO newAccountDebitTransactionDTO,
                                                         String accountNumber,
                                                         TransactionTypeEnum transactionType,
                                                         String destinationAccountNumber) {
        AccountEntity accountEntity = getAccountEntity(accountNumber);
        CardEntity cardEntity = getCardEntity(accountEntity, newAccountDebitTransactionDTO.getCard());

        BigDecimal currentAccountBalance = accountEntity.getBalance();
        BigDecimal transactionAmount = BigDecimal.valueOf(newAccountDebitTransactionDTO.getAmount());
        BigDecimal transactionFee = cardsFeeService.getCardFee(newAccountDebitTransactionDTO.getCard().getType());
        BigDecimal transactionFeeAmount = percentage(transactionAmount, transactionFee);
        BigDecimal totalTransactionAmount = transactionAmount.add(transactionFeeAmount);
        BigDecimal newAccountBalance = currentAccountBalance.subtract(totalTransactionAmount);

        if (isNewAccountBalancePositive(newAccountBalance)) {
            UUID transactionReference = UUID.randomUUID();
            TransactionEntity transactionEntity = generateDebitTransaction(
                    transactionType,
                    transactionAmount,
                    transactionFeeAmount,
                    transactionFee,
                    totalTransactionAmount,
                    accountEntity,
                    newAccountBalance,
                    cardEntity,
                    transactionReference);
            accountEntity.setBalance(newAccountBalance);
            saveAccountEntityChanges(accountEntity);
            TransactionEntity persistedTransactionEntity = transactionsRepository.save(transactionEntity);

            if (transactionType.equals(TransactionTypeEnum.TRANSFER)) {
                NewAccountCreditTransactionDTO newAccountCreditTransactionDTO =
                        generateNewAccountCreditTransactionDTO(newAccountDebitTransactionDTO);
                processCreditTransaction(newAccountCreditTransactionDTO, destinationAccountNumber, transactionReference);
            }

            logTransaction(transactionEntity);
            return toTransactionReceiptDTO(persistedTransactionEntity);
        }
        throw new BusinessException("Your account does not have enough funds to complete this operation.");
    }

    private CardEntity getCardEntity(AccountEntity accountEntity, CardDTO card) {
        return cardsRepository.findCardsByAccount(accountEntity)
                .stream()
                .filter(c ->
                        card.getNumber()
                                .equals(encryptionService.decrypt(c.getNumber()))
                                && card.getSecurityCode()
                                .equals(encryptionService.decrypt(c.getCvv()))
                ).
                findFirst()
                .orElseThrow(() -> new BadRequestException("Card data is invalid. Please check and try again."));
    }

    private AccountEntity getAccountEntity(String accountNumber) {
        return accountsRepository.findByNumber(accountNumber).orElseThrow(
                () -> new AccountNotFoundException("The account number informed is not a valid one."));
    }

    private void saveAccountEntityChanges(AccountEntity accountEntity) {
        accountsRepository.saveAccount(accountEntity);
    }

    private static boolean isNewAccountBalancePositive(BigDecimal newAccountBalance) {
        // checking if new account balance is 0 or positive
        return newAccountBalance.signum() > ANY_NEGATIVE_NUMBER;
    }

    private static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(ONE_HUNDRED);
    }

    private static void logTransaction(TransactionEntity transactionEntity) {
        log.info("Transaction persisted: {}, Account: {}, Type: {}",
                transactionEntity.getRefTransaction(),
                transactionEntity.getAccount().getNumber(),
                transactionEntity.getType());
    }
}
