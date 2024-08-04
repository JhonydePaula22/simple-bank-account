package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.AdminApi;
import com.wearewaes.model.CardFeeDTO;
import com.wearewaes.simple_bank_account.domain.services.CardsFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController extends ControllerAdvice implements AdminApi {

    private final CardsFeeService cardsFeeService;

    public AdminController(CardsFeeService cardsFeeService) {
        this.cardsFeeService = cardsFeeService;
    }

    @Override
    public ResponseEntity<CardFeeDTO> updateCardFee(CardFeeDTO cardFeeDTO) {
        return ResponseEntity.ok(cardsFeeService.updateCardFee(cardFeeDTO));
    }
}
