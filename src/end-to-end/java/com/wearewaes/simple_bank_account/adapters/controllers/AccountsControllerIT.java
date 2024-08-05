package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.simple_bank_account.TestSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

import static com.wearewaes.model.CardTypeEnum.CREDIT;
import static com.wearewaes.model.CardTypeEnum.DEBIT;
import static com.wearewaes.simple_bank_account.TestConstants.ACCOUNTS_PATH;
import static com.wearewaes.simple_bank_account.TestConstants.ACCOUNT_NUMBER_HEADER;
import static com.wearewaes.simple_bank_account.TestUtils.generateNewAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Accounts API")
class AccountsControllerIT extends TestSetup {


    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("creating account")
    class CreatingAccount {
        @Test
        @DisplayName("with debit card")
        void testCreateAccountWithoutCreditCard() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(false, "Hfid*(&80709");

            String dtoJson = objectMapper.writeValueAsString(newAccount);

            mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(newAccount.getHolder(), response.getHolder());
                        assertFalse(response.getCards().isEmpty());
                        assertEquals(1, response.getCards().size());
                        assertEquals(DEBIT, response.getCards().getFirst().getType());
                        assertNotNull(response.getNumber());
                    });
        }

        @Test
        @DisplayName("with debit and credit card")
        void testCreateAccountWithCreditCard() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(true, "&*(hGUYFy8");

            String dtoJson = objectMapper.writeValueAsString(newAccount);

            mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(newAccount.getHolder(), response.getHolder());
                        assertFalse(response.getCards().isEmpty());
                        assertEquals(2, response.getCards().size());
                        assertTrue(response.getCards().stream().anyMatch(cardDTO -> cardDTO.getType().equals(DEBIT)));
                        assertTrue(response.getCards().stream().anyMatch(cardDTO -> cardDTO.getType().equals(CREDIT)));
                        assertNotNull(response.getNumber());
                    });
        }

        @Test
        @DisplayName("account holder already exists. bad request")
        void testBadRequestCreateAccountWithUserAlreadyRegistered() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(false, "12345");

            String dtoJson = objectMapper.writeValueAsString(newAccount);

            mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("Failed to persist the user. Please check your data. " +
                                "If you already have an account, you may not create a new one!", response.getDetail());
                        assertEquals(URI.create(ACCOUNTS_PATH), response.getInstance());
                        assertEquals("Invalid request", response.getTitle());
                    });
        }
    }


    @Nested
    @DisplayName("Get Account data operations")
    class GetAccountOperations {

        @Test
        @DisplayName("get a single account data")
        void testGetAccount() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(true, "(&ˆTGY&F&GYIO");

            String dtoJson = objectMapper.writeValueAsString(newAccount);

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoJson = mvcResult.getResponse().getContentAsString();
            AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTO, response);
                    });
        }

        @Test
        @DisplayName("account number does not exists. bad request")
        void testGetAccountBadRequestWrongAccountNumber() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, "invalid_acc_number")
                    )
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("The account number informed is not a valid one.", response.getDetail());
                        assertEquals(URI.create(ACCOUNTS_PATH), response.getInstance());
                        assertEquals("Account not found", response.getTitle());
                    });
        }
    }
}