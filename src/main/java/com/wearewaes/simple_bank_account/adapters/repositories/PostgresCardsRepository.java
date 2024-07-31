package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostgresCardsRepository implements CardsRepository {

    private final SpringJDDBCCardsRepository springJDDBCCardsRepository;

    public PostgresCardsRepository(SpringJDDBCCardsRepository springJDDBCCardsRepository) {
        this.springJDDBCCardsRepository = springJDDBCCardsRepository;
    }

    @Override
    public CardEntity saveCard(CardEntity cardEntity) {
        return springJDDBCCardsRepository.save(cardEntity);
    }

    @Override
    public List<CardEntity> findCardsByAccount(AccountEntity accountEntity) {
        return springJDDBCCardsRepository.findByAccount(accountEntity);
    }
}
