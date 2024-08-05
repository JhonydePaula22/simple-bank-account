package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.CardFeeDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.TestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.wearewaes.simple_bank_account.TestUtils.generateNewAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Admin API")
class AdminControllerIT extends TestSetup {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("Get Account data operations - admin")
    class GetAccountOperations {

        @Test
        @DisplayName("get a all accounts balance paginated")
        void testGetAllAccountsBalance() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(true, "GKUYTÎˆ*GF%&");

            String dtoJson = objectMapper.writeValueAsString(newAccount);

            mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            mockMvc.perform(MockMvcRequestBuilders.get("/admin/accounts/balance")
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountsBalanceDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountsBalanceDTO.class);

                        assertNotNull(response);
                        assertNotNull(response.getPageDetails());
                        assertTrue(response.getPageDetails().getFirst());
                        assertTrue(response.getPageDetails().getLast());
                        assertEquals(20, response.getPageDetails().getSize());
                        assertEquals(0, response.getPageDetails().getNumber());
                        assertFalse(response.getAccountsBalance().isEmpty());
                        assertNotNull(response.getAccountsBalance().getFirst().getAccountNumber());
                        assertEquals(0, response.getAccountsBalance().getFirst().getBalance());
                    });
        }
    }

    @Nested
    @DisplayName("Update Fee Operation - admin")
    class UpdateFeeOperations {
        @Test
        @DisplayName("perform a fee update")
        void testGetAllAccountsBalance() throws Exception {
            CardFeeDTO cardFeeDTO = new CardFeeDTO();
            cardFeeDTO.setFee(2.0);
            cardFeeDTO.setType(CardTypeEnum.CREDIT);

            String dtoJson = objectMapper.writeValueAsString(cardFeeDTO);

            mockMvc.perform(MockMvcRequestBuilders.put("/admin/cards/fee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson)
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        CardFeeDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), CardFeeDTO.class);

                        assertNotNull(response);
                        assertEquals(cardFeeDTO, response);
                    });
        }
    }
}