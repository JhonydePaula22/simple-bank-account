package com.wearewaes.simple_bank_account.config;

import com.wearewaes.simple_bank_account.domain.commands.AccountsCommands;
import com.wearewaes.simple_bank_account.domain.commands.TransactionsCommands;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.TransactionsRepository;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;
import com.wearewaes.simple_bank_account.domain.services.TransactionsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class SpringConfig {
    @Bean
    public AccountsService accountsService(AccountsRepository accountsRepository,
                                           AccountHoldersRepository accountHoldersRepository,
                                           CardsRepository cardsRepository) {
        return new AccountsService(accountsRepository, accountHoldersRepository, cardsRepository);
    }

    @Bean
    @DependsOn("accountsService")
    public AccountsCommands accountCommands(AccountsService accountsService) {
        return new AccountsCommands(accountsService);
    }

    @Bean
    @DependsOn("accountsService")
    public TransactionsService transactionsService(TransactionsRepository transactionsRepository,
                                                   AccountsRepository accountsRepository) {
        return new TransactionsService(transactionsRepository, accountsRepository);
    }

    @Bean
    @DependsOn("transactionsService")
    public TransactionsCommands transactionsCommands(TransactionsService transactionsService) {
        return new TransactionsCommands(transactionsService);
    }
}
