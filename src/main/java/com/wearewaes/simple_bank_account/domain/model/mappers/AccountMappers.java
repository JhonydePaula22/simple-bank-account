package com.wearewaes.simple_bank_account.domain.model.mappers;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.CardDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.model.PageDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public class AccountMappers {

    public static AccountDTO toDtoMapper(AccountEntity accountEntity, List<CardEntity> cardEntities) {
        AccountHolderDTO accountHolderDTO = new AccountHolderDTO();
        accountHolderDTO.setId(accountEntity.getHolder().getIdentification());
        accountHolderDTO.setFirstName(accountEntity.getHolder().getFirstName());
        accountHolderDTO.setLastName(accountEntity.getHolder().getLastName());
        accountHolderDTO.setEmail(accountEntity.getHolder().getEmail());
        accountHolderDTO.setPhone(accountEntity.getHolder().getPhone());
        accountHolderDTO.setAddress(accountEntity.getHolder().getAddress());

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setHolder(accountHolderDTO);
        accountDTO.setNumber(accountEntity.getNumber());
        accountDTO.setBalance(accountEntity.getBalance().doubleValue());
        accountDTO.setCards(cardEntities.stream().map(
                cardEntity -> {
                    CardDTO cardDTO = new CardDTO();
                    cardDTO.setNumber(cardEntity.getNumber().toString());
                    cardDTO.setType(cardEntity.getType());
                    cardDTO.setSecurityCode(cardEntity.getCvv());
                    return cardDTO;
                }
        ).toList());

        return accountDTO;
    }

    public static AccountHolderEntity toAccountEntityHolder(NewAccountDTO newAccountDTO) {
        return new AccountHolderEntity(
                null,
                newAccountDTO.getHolder().getId(),
                newAccountDTO.getHolder().getFirstName(),
                newAccountDTO.getHolder().getLastName(),
                newAccountDTO.getHolder().getEmail(),
                newAccountDTO.getHolder().getPhone(),
                newAccountDTO.getHolder().getAddress()
        );
    }

    public static AccountEntity toAccountEntityMapper(AccountHolderEntity accountHolderEntity, String accountNumber) {
        return new AccountEntity(null,
                accountHolderEntity,
                accountNumber,
                BigDecimal.ZERO
        );
    }

    public static PageDTO toPageDto(Page<AccountEntity> accountEntities) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setFirst(accountEntities.isFirst());
        pageDTO.setLast(accountEntities.isLast());
        pageDTO.setNumber(accountEntities.getNumber());
        pageDTO.setTotalPages(accountEntities.getTotalPages());
        pageDTO.setTotalElements(accountEntities.getTotalElements());
        pageDTO.setSize(accountEntities.getSize());
        return pageDTO;
    }
}
