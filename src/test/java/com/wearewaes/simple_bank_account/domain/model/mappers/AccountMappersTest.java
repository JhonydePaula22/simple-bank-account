package com.wearewaes.simple_bank_account.domain.model.mappers;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountHolderDTO;
import com.wearewaes.model.CardDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.AccountHolderEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.services.EncryptionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMappersTest {
    private EncryptionService encryptionService =
            new EncryptionService("5lyi1fhGSeoBrI0+qERnWBUJmitWJ9IX3GVCYqANmt4=");

    @Test
    void testToDtoMapper() {
        // Setup
        AccountHolderEntity holderEntity = new AccountHolderEntity(
                UUID.randomUUID(), "123456", "John", "Doe", "john.doe@example.com", "1234567890", "123 Elm Street");

        AccountEntity accountEntity = new AccountEntity(
                UUID.randomUUID(), holderEntity, "12345", BigDecimal.valueOf(1000.50)
        );

        CardEntity cardEntity1 = new CardEntity(encryptionService.encrypt("1"), encryptionService.encrypt("123"), CardTypeEnum.DEBIT, accountEntity );
        CardEntity cardEntity2 = new CardEntity(encryptionService.encrypt("2"), encryptionService.encrypt("321"), CardTypeEnum.CREDIT, accountEntity );

        List<CardEntity> cardEntities = List.of(cardEntity1, cardEntity2);


        // Execute
        AccountDTO result = AccountMappers.toDtoMapper(accountEntity, cardEntities, encryptionService);

        // Verify
        AccountHolderDTO holderDTO = result.getHolder();
        assertThat(holderDTO.getId()).isEqualTo("123456");
        assertThat(holderDTO.getFirstName()).isEqualTo("John");
        assertThat(holderDTO.getLastName()).isEqualTo("Doe");
        assertThat(holderDTO.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(holderDTO.getPhone()).isEqualTo("1234567890");
        assertThat(holderDTO.getAddress()).isEqualTo("123 Elm Street");
        assertThat(result.getNumber()).isEqualTo("12345");
        assertThat(result.getBalance()).isEqualTo(1000.50);
        assertThat(result.getCards()).hasSize(2);

        CardDTO cardDTO1 = result.getCards().get(0);
        assertThat(cardDTO1.getNumber()).isEqualTo("1");
        assertThat(cardDTO1.getType()).isEqualTo(CardTypeEnum.DEBIT);
        assertThat(cardDTO1.getSecurityCode()).isEqualTo("123");

        CardDTO cardDTO2 = result.getCards().get(1);
        assertThat(cardDTO2.getNumber()).isEqualTo("2");
        assertThat(cardDTO2.getType()).isEqualTo(CardTypeEnum.CREDIT);
        assertThat(cardDTO2.getSecurityCode()).isEqualTo("321");
    }

    @Test
    void testToAccountEntityHolder() {
        // Setup
        AccountHolderDTO holderDTO = new AccountHolderDTO();
        holderDTO.setId("123456");
        holderDTO.setFirstName("John");
        holderDTO.setLastName("Doe");
        holderDTO.setEmail("john.doe@example.com");
        holderDTO.setPhone("1234567890");
        holderDTO.setAddress("123 Elm Street");

        NewAccountDTO newAccountDTO = new NewAccountDTO();
        newAccountDTO.setHolder(holderDTO);

        // Execute
        AccountHolderEntity result = AccountMappers.toAccountEntityHolder(newAccountDTO);

        // Verify
        assertThat(result.getIdentification()).isEqualTo("123456");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getPhone()).isEqualTo("1234567890");
        assertThat(result.getAddress()).isEqualTo("123 Elm Street");
    }

    @Test
    void testToAccountEntityMapper() {
        // Setup
        AccountHolderEntity holderEntity = new AccountHolderEntity(
                UUID.randomUUID(), "123456", "John", "Doe", "john.doe@example.com", "1234567890", "123 Elm Street");

        // Execute
        AccountEntity result = AccountMappers.toAccountEntityMapper(holderEntity, "12345");

        // Verify
        assertThat(result.getHolder()).isEqualTo(holderEntity);
        assertThat(result.getNumber()).isEqualTo("12345");
        assertThat(result.getBalance()).isEqualTo(BigDecimal.ZERO);
    }
}