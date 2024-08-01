package com.wearewaes.simple_bank_account.config;

import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;
import com.wearewaes.simple_bank_account.domain.services.CardsServiceFactoryService;
import com.wearewaes.simple_bank_account.domain.services.CreateCreditCardService;
import com.wearewaes.simple_bank_account.domain.services.CreateDebitCardService;
import com.wearewaes.simple_bank_account.domain.services.GetAccountService;
import com.wearewaes.simple_bank_account.domain.services.TransactionsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringConfig {

    @Bean
    public CreateCreditCardService createCreditCardService(CardsRepository cardsRepository) {
        return new CreateCreditCardService(cardsRepository);
    }

    @Bean CreateDebitCardService createDebitCardService(CardsRepository cardsRepository) {
        return new CreateDebitCardService(cardsRepository);
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
                                           CardsServiceFactoryService cardsServiceFactoryService) {
        return new AccountsService(accountsRepository, accountHoldersRepository, cardsServiceFactoryService);
    }

    @Bean
    public TransactionsService transactionsService(TransactionsRepository transactionsRepository,
                                                   AccountsRepository accountsRepository,
                                                   CardsRepository cardsRepository) {
        return new TransactionsService(transactionsRepository, accountsRepository, cardsRepository);
    }

    @Bean
    public GetAccountService getAccountService(AccountsRepository accountsRepository,
                                               CardsRepository cardsRepository) {
        return new GetAccountService(accountsRepository, cardsRepository);
    }
}
