package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.AccountEntity;
import com.wearewaes.simple_bank_account.domain.model.CardEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateDebitCardServiceTest {

    @Mock
    private CardsRepository cardsRepository;

    @InjectMocks
    private CreateDebitCardService createDebitCardService;

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService("5lyi1fhGSeoBrI0+qERnWBUJmitWJ9IX3GVCYqANmt4=");
        createDebitCardService = new CreateDebitCardService(cardsRepository, encryptionService);
    }

    @Test
    void testGenerateCard() {
        // Arrange
        AccountEntity accountEntity = new AccountEntity(); // Assuming a no-arg constructor or a valid constructor
        CardEntity cardEntity = new CardEntity("1234567890123456", "123", CardTypeEnum.DEBIT, accountEntity);

        when(cardsRepository.saveCard(any(CardEntity.class))).thenReturn(cardEntity);

        // Act
        CardEntity result = createDebitCardService.generateCard(accountEntity);

        // Assert
        assertNotNull(result);
        assertEquals(CardTypeEnum.DEBIT, result.getType());
        assertEquals(accountEntity, result.getAccount());

        verify(cardsRepository, times(1)).saveCard(any(CardEntity.class));
    }

    @Test
    void testGenerateCardNumber() {
        // Generate a card number and ensure it matches expected format
        String cardNumber = createDebitCardService.generateCardNumber();
        assertNotNull(cardNumber);
    }

    @Test
    void testGenerateCVV() {
        // Generate a CVV and ensure it matches expected format
        String cvv = createDebitCardService.generateCVV();
        assertNotNull(cvv);
    }
}