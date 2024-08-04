package com.wearewaes.simple_bank_account.config;

import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsFeeRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;
import com.wearewaes.simple_bank_account.domain.services.CardsFeeService;
import com.wearewaes.simple_bank_account.domain.services.CardsServiceFactoryService;
import com.wearewaes.simple_bank_account.domain.services.CreateCreditCardService;
import com.wearewaes.simple_bank_account.domain.services.CreateDebitCardService;
import com.wearewaes.simple_bank_account.domain.services.EncryptionService;
import com.wearewaes.simple_bank_account.domain.services.GetAccountService;
import com.wearewaes.simple_bank_account.domain.services.TransactionsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringConfig {


    @Value("${security.encryption.key}")
    private String encryptionKey;

    @Bean
    public CreateCreditCardService createCreditCardService(CardsRepository cardsRepository,
                                                           EncryptionService encryptionService) {
        return new CreateCreditCardService(cardsRepository, encryptionService);
    }

    @Bean CreateDebitCardService createDebitCardService(CardsRepository cardsRepository,
                                                        EncryptionService encryptionService) {
        return new CreateDebitCardService(cardsRepository, encryptionService);
    }

    @Bean
    @DependsOn({"createDebitCardService", "createCreditCardService"})
    public CardsServiceFactoryService cardsFactory(CreateCreditCardService createCreditCardService,
                                                   CreateDebitCardService createDebitCardService) {
        return new CardsServiceFactoryService(createDebitCardService, createCreditCardService);
    }

    @Bean
    @DependsOn("cardsFactory")
    public AccountsService accountsService(AccountsRepository accountsRepository,
                                           AccountHoldersRepository accountHoldersRepository,
                                           CardsServiceFactoryService cardsServiceFactoryService,
                                           EncryptionService encryptionService) {
        return new AccountsService(accountsRepository, accountHoldersRepository,
                cardsServiceFactoryService, encryptionService);
    }

    @Bean
    @DependsOn("cardsFeeService")
    public TransactionsService transactionsService(TransactionsRepository transactionsRepository,
                                                   AccountsRepository accountsRepository,
                                                   CardsRepository cardsRepository,
                                                   EncryptionService encryptionService,
                                                   CardsFeeService cardsFeeFactory) {
        return new TransactionsService(transactionsRepository,
                accountsRepository,
                cardsRepository,
                encryptionService,
                cardsFeeFactory);
    }

    @Bean
    public GetAccountService getAccountService(AccountsRepository accountsRepository,
                                               CardsRepository cardsRepository,
                                               EncryptionService encryptionService) {
        return new GetAccountService(accountsRepository, cardsRepository, encryptionService);
    }

    @Bean
    public CardsFeeService cardsFeeService(CardsFeeRepository cardsFeeRepository) {
        return new CardsFeeService(cardsFeeRepository);
    }

    @Bean
    public EncryptionService encryptionService() {
        return new EncryptionService(encryptionKey);
    }
}
