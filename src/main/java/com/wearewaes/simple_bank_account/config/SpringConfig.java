package com.wearewaes.simple_bank_account.config;

import com.wearewaes.simple_bank_account.domain.commands.AccountCommands;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountHoldersRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import com.wearewaes.simple_bank_account.domain.services.AccountsService;
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
    public AccountCommands accountCommands(AccountsService accountsService) {
        return new AccountCommands(accountsService);
    }
}
