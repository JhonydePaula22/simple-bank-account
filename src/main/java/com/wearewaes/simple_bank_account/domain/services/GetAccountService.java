package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.AccountBalanceDTO;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.PageDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.model.exceptions.AccountNotFoundException;
import com.wearewaes.simple_bank_account.domain.ports.repositories.AccountsRepository;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.wearewaes.simple_bank_account.domain.model.mappers.AccountMappers.toPageDto;
import static com.wearewaes.simple_bank_account.domain.model.mappers.AccountMappers.toDtoMapper;

public class GetAccountService {

    private final AccountsRepository accountsRepository;
    private final CardsRepository cardsRepository;
    private final EncryptionService encryptionService;

    public GetAccountService(AccountsRepository accountsRepository, CardsRepository cardsRepository,
                             EncryptionService encryptionService) {
        this.accountsRepository = accountsRepository;
        this.cardsRepository = cardsRepository;
        this.encryptionService = encryptionService;
    }


    public AccountDTO getAccount(String accountNumber) {
        AccountEntity accountEntity = accountsRepository.findByNumber(accountNumber).orElseThrow(
                () -> new AccountNotFoundException("The account number informed is not a valid one."));
        List<CardEntity> cards = cardsRepository.findCardsByAccount(accountEntity);
        return toDtoMapper(accountEntity, cards, encryptionService);
    }


    public AccountsBalanceDTO getAllAccountsBalance(Integer offset, Integer limit) {
        Page<AccountEntity> accountEntities = accountsRepository.findAll(PageRequest.of(offset, limit));
        PageDTO pageDTO = toPageDto(accountEntities);
        AccountsBalanceDTO accountsBalanceDTO = new AccountsBalanceDTO();
        accountsBalanceDTO.setPageDetails(pageDTO);
        accountsBalanceDTO.setAccountsBalance(accountEntities.map(accountEntity -> {
            AccountBalanceDTO accountBalanceDTO = new AccountBalanceDTO();
            accountBalanceDTO.setAccountNumber(accountEntity.getNumber());
            accountBalanceDTO.setBalance(accountEntity.getBalance().doubleValue());
            return accountBalanceDTO;
        }).stream().toList());
        return accountsBalanceDTO;
    }
}
