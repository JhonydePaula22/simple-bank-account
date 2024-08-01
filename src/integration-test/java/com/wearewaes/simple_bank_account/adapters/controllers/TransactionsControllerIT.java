package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.TestContainersSetUp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.wearewaes.simple_bank_account.TestUtils.generateNewAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionsControllerIT extends TestContainersSetUp {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDepositMoneyToAccountAndVerifyIfAccountBalanceIsUpdated() throws Exception {
        var newAccount = generateNewAccount(false, "Hfid*(&80709");

        var newAccountDtoJson = objectMapper.writeValueAsString(newAccount);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountDtoJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String accountDtoJson = mvcResult.getResponse().getContentAsString();
        AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(100.00);

        var depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositDtoJson)
                        .header("account_number", accountDTO.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    TransactionReceiptDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                    assertNotNull(response);
                    assertNotNull(response.getTimestamp());
                    assertNotNull(response.getId());
                });

        accountDTO.setBalance(100.00);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTO.getNumber())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    AccountDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                    assertNotNull(response);
                    assertEquals(accountDTO, response);
                });

    }
}