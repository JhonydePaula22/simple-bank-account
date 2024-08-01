package com.wearewaes.simple_bank_account.adapters.controllers;

import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.TestContainersSetUp;
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
        var newAccount = generateNewAccount(false, "Hfid*(&8070fhjdsiah");

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

    @Test
    void testWithdrawMoneyWithDebitCardFromAccountAndVerifyIfAccountBalanceIsUpdated() throws Exception {
        var newAccount = generateNewAccount(false, "Hfi0709gfdsuoadhsahbfv");

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
                ).andExpect(MockMvcResultMatchers.status().isOk());

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(50.0);
        debitTransactionDTO.setCard(accountDTO.getCards().stream()
                .filter(c-> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawDtoJson)
                        .header("account_number", accountDTO.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    TransactionReceiptDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                    assertNotNull(response);
                    assertNotNull(response.getTimestamp());
                    assertNotNull(response.getId());
                });

        accountDTO.setBalance(50.00);

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

    @Test
    void testWithdrawMoneyWithCreditFromAccountAndVerifyIfAccountBalanceIsUpdated() throws Exception {
        var newAccount = generateNewAccount(true, "BHDCsaud*(&80709");

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
                ).andExpect(MockMvcResultMatchers.status().isOk());

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(50.0);
        debitTransactionDTO.setCard(accountDTO.getCards().stream()
                .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawDtoJson)
                        .header("account_number", accountDTO.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    TransactionReceiptDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                    assertNotNull(response);
                    assertNotNull(response.getTimestamp());
                    assertNotNull(response.getId());
                });

        accountDTO.setBalance(49.50);

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

    @Test
    void testTransferMoneyWithDebitCardFromAccountAndVerifyIfAccountsBalancesAreUpdated() throws Exception {
        var newAccountOrigin = generateNewAccount(false, "Hfi&*(TGY&80709");
        var newAccountDestination = generateNewAccount(false, "Hfi*()&80708");

        var newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
        var newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountOriginJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
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

        var depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositDtoJson)
                .header("account_number", accountDTOOrigin.getNumber())
        ).andExpect(MockMvcResultMatchers.status().isOk());

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(50.0);
        debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                .filter(c-> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawDtoJson)
                        .header("account_number", accountDTOOrigin.getNumber())
                        .header("destination_account_number", accountDTODestination.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    TransactionReceiptDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                    assertNotNull(response);
                    assertNotNull(response.getTimestamp());
                    assertNotNull(response.getId());
                });

        accountDTOOrigin.setBalance(50.00);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTOOrigin.getNumber())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    AccountDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                    assertNotNull(response);
                    assertEquals(accountDTOOrigin, response);
                });

        accountDTODestination.setBalance(50.00);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTODestination.getNumber())
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
    void testTransferMoneyWithCreditCardFromAccountAndVerifyIfAccountsBalancesAreUpdated() throws Exception {
        var newAccountOrigin = generateNewAccount(true, "HfiXFH709");
        var newAccountDestination = generateNewAccount(false, "HfiNJKNL0708");

        var newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
        var newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountOriginJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
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

        var depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositDtoJson)
                .header("account_number", accountDTOOrigin.getNumber())
        ).andExpect(MockMvcResultMatchers.status().isOk());

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(50.0);
        debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawDtoJson)
                        .header("account_number", accountDTOOrigin.getNumber())
                        .header("destination_account_number", accountDTODestination.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    TransactionReceiptDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), TransactionReceiptDTO.class);
                    assertNotNull(response);
                    assertNotNull(response.getTimestamp());
                    assertNotNull(response.getId());
                });

        accountDTOOrigin.setBalance(49.50);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTOOrigin.getNumber())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    AccountDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                    assertNotNull(response);
                    assertEquals(accountDTOOrigin, response);
                });

        accountDTODestination.setBalance(50.00);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTODestination.getNumber())
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
    void testTransferMoneyWithCreditBadRequestWrongCardDetailsNoBalanceUpdateAfterBadRequest() throws Exception {
        var newAccountOrigin = generateNewAccount(true, "Hfi%&*09");
        var newAccountDestination = generateNewAccount(false, "HVGHJd*(&80708");

        var newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
        var newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountOriginJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
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

        var depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositDtoJson)
                .header("account_number", accountDTOOrigin.getNumber())
        ).andExpect(MockMvcResultMatchers.status().isOk());

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(50.0);
        debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                .filter(c-> c.getType().equals(CardTypeEnum.CREDIT)).findFirst().get());
        debitTransactionDTO.getCard().setNumber("123454325432");

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawDtoJson)
                        .header("account_number", accountDTOOrigin.getNumber())
                        .header("destination_account_number", accountDTODestination.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> {
                    ProblemDetail response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                    assertNotNull(response);
                    assertEquals("Card data is invalid. Please check and try again.", response.getDetail());
                    assertEquals(URI.create("/transactions/transfer"), response.getInstance());
                    assertEquals("Invalid data on the request", response.getTitle());
                });

        accountDTOOrigin.setBalance(100.00);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTOOrigin.getNumber())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    AccountDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                    assertNotNull(response);
                    assertEquals(accountDTOOrigin.getBalance(), response.getBalance());
                });

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTODestination.getNumber())
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
    void testTransferMoneyWithDebitBadRequestWrongDestinationAccountNoBalanceUpdateAfterBadRequest() throws Exception {
        var newAccountOrigin = generateNewAccount(false, "HfNKJLBJLK&80709");
        var newAccountDestination = generateNewAccount(false, "HfCRTFT80708");

        var newAccountOriginJson = objectMapper.writeValueAsString(newAccountOrigin);
        var newAccountDestinationJson = objectMapper.writeValueAsString(newAccountDestination);

        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountOriginJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        MvcResult mvcResult2 = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountDestinationJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String accountDtoOriginJson = mvcResult1.getResponse().getContentAsString();
        String accountDtoDestinationJson = mvcResult2.getResponse().getContentAsString();

        AccountDTO accountDTOOrigin = objectMapper.readValue(accountDtoOriginJson, AccountDTO.class);
        AccountDTO accountDTODestination = objectMapper.readValue(accountDtoDestinationJson, AccountDTO.class);
        var correctDestinationAccount = accountDTODestination.getNumber();
        accountDTODestination.setNumber("invalid");

        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(100.00);

        var depositDtoJson = objectMapper.writeValueAsString(newAccountCreditTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositDtoJson)
                .header("account_number", accountDTOOrigin.getNumber())
        ).andExpect(MockMvcResultMatchers.status().isOk());

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(50.0);
        debitTransactionDTO.setCard(accountDTOOrigin.getCards().stream()
                .filter(c-> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawDtoJson)
                        .header("account_number", accountDTOOrigin.getNumber())
                        .header("destination_account_number", accountDTODestination.getNumber())
                ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> {
                    ProblemDetail response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), ProblemDetail.class);

                    assertNotNull(response);
                    assertEquals("The account number informed is not a valid one.", response.getDetail());
                    assertEquals(URI.create("/transactions/transfer"), response.getInstance());
                    assertEquals("Account not found", response.getTitle());
                });

        accountDTOOrigin.setBalance(100.00);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTOOrigin.getNumber())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    AccountDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                    assertNotNull(response);
                    assertEquals(accountDTOOrigin.getBalance(), response.getBalance());
                });

        accountDTODestination.setNumber(correctDestinationAccount);

        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("account_number", accountDTODestination.getNumber())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    AccountDTO response = objectMapper
                            .readValue(result.getResponse().getContentAsString(), AccountDTO.class);

                    assertNotNull(response);
                    assertEquals(accountDTODestination.getBalance(), response.getBalance());
                });
    }
}