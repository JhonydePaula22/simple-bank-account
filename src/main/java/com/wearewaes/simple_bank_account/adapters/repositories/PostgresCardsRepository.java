package com.wearewaes.simple_bank_account.adapters.repositories;

import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostgresCardsRepository implements CardsRepository {

    private final SpringJPACardsRepository springJPACardsRepository;

    public PostgresCardsRepository(SpringJPACardsRepository springJPACardsRepository) {
        this.springJPACardsRepository = springJPACardsRepository;
    }

    @Override
    public CardEntity saveCard(CardEntity cardEntity) {
        return springJPACardsRepository.save(cardEntity);
    }

    @Override
    public List<CardEntity> findCardsByAccount(AccountEntity accountEntity) {
        return springJPACardsRepository.findByAccount(accountEntity);
    }
}
