package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CardsServiceFactoryTest {

    @Mock
    private CreateDebitCardService createDebitCardService;

    @Mock
    private CreateCreditCardService createCreditCardService;

    @InjectMocks
    private CardsServiceFactory cardsServiceFactory;

    @BeforeEach
    void setUp() {
        cardsServiceFactory = new CardsServiceFactory(createDebitCardService, createCreditCardService);
    }

    @Test
    void testGetCardGeneratorForDebitCard() {
        CardGenerator cardGenerator = cardsServiceFactory.getCardGenerator(CardTypeEnum.DEBIT);
        assertEquals(createDebitCardService, cardGenerator);
    }

    @Test
    void testGetCardGeneratorForCreditCard() {
        CardGenerator cardGenerator = cardsServiceFactory.getCardGenerator(CardTypeEnum.CREDIT);
        assertEquals(createCreditCardService, cardGenerator);
    }

}