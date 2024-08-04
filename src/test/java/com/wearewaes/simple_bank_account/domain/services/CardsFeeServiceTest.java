package com.wearewaes.simple_bank_account.domain.services;

import com.wearewaes.model.CardFeeDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.simple_bank_account.domain.model.CardFeeEntity;
import com.wearewaes.simple_bank_account.domain.ports.repositories.CardsFeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardsFeeServiceTest {

    @Mock
    private CardsFeeRepository cardsFeeRepository;

    @InjectMocks
    private CardsFeeService cardsFeeService;


    @Test
    public void testGetCreditCardFee() {
        // Arrange
        CardTypeEnum cardType = CardTypeEnum.CREDIT;
        BigDecimal expectedFee = BigDecimal.valueOf(1.0);

        CardFeeEntity cardFeeEntity = new CardFeeEntity(cardType, expectedFee);
        when(cardsFeeRepository.getFeeByType(cardType)).thenReturn(cardFeeEntity);

        // Act
        BigDecimal fee = cardsFeeService.getCardFee(cardType);

        // Assert
        assertEquals(expectedFee, fee);
        verify(cardsFeeRepository, times(1)).getFeeByType(cardType);
    }


    @Test
    public void testGetDebitCardFee() {
        // Arrange
        CardTypeEnum cardType = CardTypeEnum.DEBIT;
        BigDecimal expectedFee = BigDecimal.valueOf(0.0);

        CardFeeEntity cardFeeEntity = new CardFeeEntity(cardType, expectedFee);
        when(cardsFeeRepository.getFeeByType(cardType)).thenReturn(cardFeeEntity);

        // Act
        BigDecimal fee = cardsFeeService.getCardFee(cardType);

        // Assert
        assertEquals(expectedFee, fee);
        verify(cardsFeeRepository, times(1)).getFeeByType(cardType);
    }

    @Test
    public void testUpdateCardFee() {
        // Arrange
        CardTypeEnum cardType = CardTypeEnum.DEBIT;
        BigDecimal newFee = BigDecimal.valueOf(2.0);
        CardFeeDTO cardFeeDTO = new CardFeeDTO();
        cardFeeDTO.setType(cardType);
        cardFeeDTO.setFee(newFee.doubleValue());
        CardFeeEntity cardFeeEntity = new CardFeeEntity(cardType, newFee);
        when(cardsFeeRepository.save(any(CardFeeEntity.class))).thenReturn(cardFeeEntity);

        // Act
        CardFeeDTO updatedCardFeeDTO = cardsFeeService.updateCardFee(cardFeeDTO);

        // Assert
        assertEquals(cardFeeDTO, updatedCardFeeDTO);
        verify(cardsFeeRepository, times(1)).save(any(CardFeeEntity.class));
    }
}