package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
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

import static com.wearewaes.simple_bank_account.TestConstants.ACCOUNTS_PATH;
import static com.wearewaes.simple_bank_account.TestConstants.ACCOUNT_NUMBER_HEADER;
import static com.wearewaes.simple_bank_account.TestConstants.DESTINATION_ACCOUNT_NUMBER_HEADER;
import static com.wearewaes.simple_bank_account.TestConstants.TRANSACTIONS_DEPOSITS_PATH;
import static com.wearewaes.simple_bank_account.TestConstants.TRANSACTIONS_TRANSFERS_PATH;
import static com.wearewaes.simple_bank_account.TestConstants.TRANSACTIONS_WITHDRAWALS_PATH;
import static com.wearewaes.simple_bank_account.TestUtils.generateNewAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Transactions API")
class TransactionsControllerIT extends TestSetup {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("Deposit operations")
    class DepositOperations {

        @Test
        @DisplayName("add money to account and verify balance")
        void testDepositMoneyToAccountAndVerifyIfAccountBalanceIsUpdated() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(false);

            String newAccountDtoJson = objectMapper.writeValueAsString(newAccount);

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoJson = mvcResult.getResponse().getContentAsString();
            AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(depositDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        TransactionReceiptDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                        assertNotNull(response);
                        assertNotNull(response.getTimestamp());
                        assertNotNull(response.getId());
                    });

            accountDTO.setBalance(100.00);

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
        @DisplayName("no deposit with negative amount is allowed - bad request")
        void testDepositMoneyToAccountWithNegativeAmountReturnsBadRequest() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(false);

            String newAccountDtoJson = objectMapper.writeValueAsString(newAccount);

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoJson = mvcResult.getResponse().getContentAsString();
            AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(-100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(depositDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("The amount for this transaction must not be a smaller than zero.", response.getDetail());
                        assertEquals(URI.create(TRANSACTIONS_DEPOSITS_PATH), response.getInstance());
                        assertEquals("Invalid data on the request", response.getTitle());
                    });

            accountDTO.setBalance(0.0);

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
    }

    @Nested
    @DisplayName("withdraw operations")
    class WithdrawOperations {

        @Test
        @DisplayName("withdraw money with debit card and check balance")
        void testWithdrawMoneyWithDebitCardFromAccountAndVerifyIfAccountBalanceIsUpdated() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(false);

            String newAccountDtoJson = objectMapper.writeValueAsString(newAccount);

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoJson = mvcResult.getResponse().getContentAsString();
            AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(depositDtoJson)
                    .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
            ).andExpect(MockMvcResultMatchers.status().isOk());

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(50.0);
            debitTransactionDTO.setCard(accountDTO.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_WITHDRAWALS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        TransactionReceiptDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                        assertNotNull(response);
                        assertNotNull(response.getTimestamp());
                        assertNotNull(response.getId());
                    });

            accountDTO.setBalance(50.00);

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
        @DisplayName("withdraw money with credit card and check balance")
        void testWithdrawMoneyWithCreditFromAccountAndVerifyIfAccountBalanceIsUpdated() throws Exception {
            NewAccountDTO newAccount = generateNewAccount(true);

            String newAccountDtoJson = objectMapper.writeValueAsString(newAccount);

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDtoJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoJson = mvcResult.getResponse().getContentAsString();
            AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(depositDtoJson)
                    .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
            ).andExpect(MockMvcResultMatchers.status().isOk());

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(50.0);
            debitTransactionDTO.setCard(accountDTO.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_WITHDRAWALS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTO.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        TransactionReceiptDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                        assertNotNull(response);
                        assertNotNull(response.getTimestamp());
                        assertNotNull(response.getId());
                    });

            accountDTO.setBalance(49.50);

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
    }

    @Nested
    @DisplayName("Transfer operations")
    class TransferOperations {

        @Test
        @DisplayName("transfer money with debit card and check balance")
        void testTransferMoneyWithDebitCardFromAccountAndVerifyIfAccountsBalancesAreUpdated() throws Exception {
            NewAccountDTO newAccountOrigin = generateNewAccount(false);
            NewAccountDTO newAccountDestination = generateNewAccount(false);

            String newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
            String newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

            MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountOriginJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDestinationJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoOriginJson = mvcResult1.getResponse().getContentAsString();
            String accountDtoDestinationJson = mvcResult2.getResponse().getContentAsString();

            AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);
            AccountDTO accountDTODestination = objectMapper.readValue(accountDtoDestinationJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(depositDtoJson)
                    .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
            ).andExpect(MockMvcResultMatchers.status().isOk());

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(50.0);
            debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_TRANSFERS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                            .header(DESTINATION_ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        TransactionReceiptDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                        assertNotNull(response);
                        assertNotNull(response.getTimestamp());
                        assertNotNull(response.getId());
                    });

            accountDTOOrigin.setBalance(50.00);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTOOrigin, response);
                    });

            accountDTODestination.setBalance(50.00);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTODestination, response);
                    });
        }

        @Test
        @DisplayName("transfer money with credit card and check balance")
        void testTransferMoneyWithCreditCardFromAccountAndVerifyIfAccountsBalancesAreUpdated() throws Exception {
            NewAccountDTO newAccountOrigin = generateNewAccount(true);
            NewAccountDTO newAccountDestination = generateNewAccount(false);

            String newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
            String newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

            MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountOriginJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDestinationJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoOriginJson = mvcResult1.getResponse().getContentAsString();
            String accountDtoDestinationJson = mvcResult2.getResponse().getContentAsString();

            AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);
            AccountDTO accountDTODestination = objectMapper.readValue(accountDtoDestinationJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(depositDtoJson)
                    .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
            ).andExpect(MockMvcResultMatchers.status().isOk());

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(50.0);
            debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_TRANSFERS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                            .header(DESTINATION_ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        TransactionReceiptDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                        assertNotNull(response);
                        assertNotNull(response.getTimestamp());
                        assertNotNull(response.getId());
                    });

            accountDTOOrigin.setBalance(49.50);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTOOrigin, response);
                    });

            accountDTODestination.setBalance(50.00);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTODestination, response);
                    });
        }

        @Test
        @DisplayName("transfer money with wrong credit card details. bad request balances not changed")
        void testTransferMoneyWithCreditBadRequestWrongCardDetailsNoBalanceUpdateAfterBadRequest() throws Exception {
            NewAccountDTO newAccountOrigin = generateNewAccount(true);
            NewAccountDTO newAccountDestination = generateNewAccount(false);

            String newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
            String newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

            MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountOriginJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDestinationJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoOriginJson = mvcResult1.getResponse().getContentAsString();
            String accountDtoDestinationJson = mvcResult2.getResponse().getContentAsString();

            AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);
            AccountDTO accountDTODestination = objectMapper.readValue(accountDtoDestinationJson, AccountDTO.class);

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(depositDtoJson)
                    .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
            ).andExpect(MockMvcResultMatchers.status().isOk());

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(50.0);
            debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());
            debitTransactionDTO.getCard().setNumber("123454325432");

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_TRANSFERS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                            .header(DESTINATION_ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("Card data is invalid. Please check and try again.", response.getDetail());
                        assertEquals(URI.create(TRANSACTIONS_TRANSFERS_PATH), response.getInstance());
                        assertEquals("Invalid data on the request", response.getTitle());
                    });

            accountDTOOrigin.setBalance(100.00);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTOOrigin.getBalance(), response.getBalance());
                    });

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTODestination.getBalance(), response.getBalance());
                    });
        }

        @Test
        @DisplayName("transfer money with wrong destination account details. bad request balances not changed")
        void testTransferMoneyWithDebitBadRequestWrongDestinationAccountNoBalanceUpdateAfterBadRequest() throws Exception {
            NewAccountDTO newAccountOrigin = generateNewAccount(false);
            NewAccountDTO newAccountDestination = generateNewAccount(false);

            String newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
            String newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

            MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountOriginJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDestinationJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoOriginJson = mvcResult1.getResponse().getContentAsString();
            String accountDtoDestinationJson = mvcResult2.getResponse().getContentAsString();

            AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);
            AccountDTO accountDTODestination = objectMapper.readValue(accountDtoDestinationJson, AccountDTO.class);
            String correctDestinationAccount = accountDTODestination.getNumber();
            accountDTODestination.setNumber("invalid");

            NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
            newAccountCreditTransactionDTO.setAmount(100.00);

            String depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_DEPOSITS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(depositDtoJson)
                    .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
            ).andExpect(MockMvcResultMatchers.status().isOk());

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(50.0);
            debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_TRANSFERS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                            .header(DESTINATION_ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("The account number informed is not a valid one.", response.getDetail());
                        assertEquals(URI.create(TRANSACTIONS_TRANSFERS_PATH), response.getInstance());
                        assertEquals("Account not found", response.getTitle());
                    });

            accountDTOOrigin.setBalance(100.00);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTOOrigin.getBalance(), response.getBalance());
                    });

            accountDTODestination.setNumber(correctDestinationAccount);

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTODestination.getBalance(), response.getBalance());
                    });
        }

        @Test
        @DisplayName("no transfer money with negative amount is allowed. bad request balances not changed")
        void testTransferMoneyWithNegativeAmountNoBalanceUpdateAfterBadRequest() throws Exception {
            NewAccountDTO newAccountOrigin = generateNewAccount(true);
            NewAccountDTO newAccountDestination = generateNewAccount(false);

            String newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
            String newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

            MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountOriginJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountDestinationJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoOriginJson = mvcResult1.getResponse().getContentAsString();
            String accountDtoDestinationJson = mvcResult2.getResponse().getContentAsString();

            AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);
            AccountDTO accountDTODestination = objectMapper.readValue(accountDtoDestinationJson, AccountDTO.class);

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(-50.0);
            debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());
            debitTransactionDTO.getCard().setNumber("123454325432");

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_TRANSFERS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                            .header(DESTINATION_ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("The amount for this transaction must not be a smaller than zero.", response.getDetail());
                        assertEquals(URI.create(TRANSACTIONS_TRANSFERS_PATH), response.getInstance());
                        assertEquals("Invalid data on the request", response.getTitle());
                    });

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTOOrigin.getBalance(), response.getBalance());
                    });

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTODestination.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTODestination.getBalance(), response.getBalance());
                    });
        }

        @Test
        @DisplayName("no withdraw money with negative amount is allowed. bad request balances not changed")
        void testWithdrawMoneyWithNegativeAmountNoBalanceUpdateAfterBadRequest() throws Exception {
            NewAccountDTO newAccountOrigin = generateNewAccount(true);

            String newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);

            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountOriginJson))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn();

            String accountDtoOriginJson = mvcResult.getResponse().getContentAsString();

            AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);

            NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
            debitTransactionDTO.setAmount(-50.0);
            debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                    .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());
            debitTransactionDTO.getCard().setNumber("123454325432");

            String withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

            mockMvc.perform(MockMvcRequestBuilders.post(TRANSACTIONS_WITHDRAWALS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(withdrawDtoJson)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> {
                        ProblemDetail response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                        assertNotNull(response);
                        assertEquals("The amount for this transaction must not be a smaller than zero.", response.getDetail());
                        assertEquals(URI.create(TRANSACTIONS_WITHDRAWALS_PATH), response.getInstance());
                        assertEquals("Invalid data on the request", response.getTitle());
                    });

            mockMvc.perform(MockMvcRequestBuilders.get(ACCOUNTS_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(ACCOUNT_NUMBER_HEADER, accountDTOOrigin.getNumber())
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(result -> {
                        AccountDTO response = objectMapper
                                .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                        assertNotNull(response);
                        assertEquals(accountDTOOrigin.getBalance(), response.getBalance());
                    });
        }
    }

}