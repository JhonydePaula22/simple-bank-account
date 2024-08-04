package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.api.AdminApi;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.CardFeeDTO;
import com.wearewaes.simple_bank_account.domain.services.CardsFeeService;
import com.wearewaes.simple_bank_account.domain.services.GetAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController extends ControllerAdvice implements AdminApi {

    private final CardsFeeService cardsFeeService;
    private final GetAccountService getAccountService;

    public AdminController(CardsFeeService cardsFeeService, GetAccountService getAccountService) {
        this.cardsFeeService = cardsFeeService;
        this.getAccountService = getAccountService;
    }

    @Override
    public ResponseEntity<CardFeeDTO> updateCardFee(CardFeeDTO cardFeeDTO) {
        return ResponseEntity.ok(cardsFeeService.updateCardFee(cardFeeDTO));
    }

    @Override
    public ResponseEntity<AccountsBalanceDTO> getAllAccountsBalance(Integer offset, Integer limit) {
        return ResponseEntity.ok(getAccountService.getAllAccountsBalance(offset, limit));
    }
}
