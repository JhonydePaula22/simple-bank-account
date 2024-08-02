package com.wearewaes.simple_bank_account.high_concurrence_transactions;

import com.wearewaes.model.AccountBalanceDTO;
import com.wearewaes.model.AccountDTO;
import com.wearewaes.model.AccountsBalanceDTO;
import com.wearewaes.model.CardTypeEnum;
import com.wearewaes.model.NewAccountCreditTransactionDTO;
import com.wearewaes.model.NewAccountDebitTransactionDTO;
import com.wearewaes.model.TransactionReceiptDTO;
import com.wearewaes.simple_bank_account.TestSetup;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.wearewaes.simple_bank_account.TestUtils.generateNewAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
public class HighConcurrencyTransactionsIT extends TestSetup {

    @Autowired
    private MockMvc mockMvc;

    private List<AccountDTO> accounts = new ArrayList<>();
    private static final int NUM_ACCOUNTS = 10;
    private static final double INITIAL_BALANCE = 1000.00;
    private static final int NUM_TRANSACTIONS = 1000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testHighConcurrencyTransactions() throws Exception {
        for (int i = 0; i < NUM_ACCOUNTS; i++) {
            AccountDTO account = createAccountDTOAndDepositMoney("id_" + i, INITIAL_BALANCE);
            accounts.add(account);
        }

        double initialTotalBalance;

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Random random = new Random();
            initialTotalBalance = accounts.stream().mapToDouble(AccountDTO::getBalance).sum();

            // Submit tasks to executor service
            Future<?>[] futures = new Future[NUM_TRANSACTIONS];
            for (int i = 0; i < NUM_TRANSACTIONS; i++) {
                futures[i] = executorService.submit(() -> {
                    int fromIndex = random.nextInt(NUM_ACCOUNTS);
                    int toIndex;
                    do {
                        toIndex = random.nextInt(NUM_ACCOUNTS);
                    } while (toIndex == fromIndex);

                    double amount = random.nextDouble() * 100;
                    try {
                        transferMoney(accounts.get(fromIndex), accounts.get(toIndex), amount);
                    } catch (Exception e) {
                        // Handle exception if necessary (e.g., InsufficientFundsException)
                    }
                });
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }

            executorService.shutdown();
        }

        // Verify that the total balance remains the same
        assertEquals(initialTotalBalance, getAllAccountsBalanceSum(), 0.01); // Allow for minor floating-point variations
    }

    private AccountDTO createAccountDTOAndDepositMoney(String identification, Double amount) throws Exception {
        var newAccount = generateNewAccount(false, identification);

        var newAccountDtoJson = objectMapper.writeValueAsString(newAccount);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountDtoJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String accountDtoJson = mvcResult.getResponse().getContentAsString();
        AccountDTO accountDTO = objectMapper.readValue(accountDtoJson, AccountDTO.class);

        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(amount);

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
                }).andReturn();
        accountDTO.setBalance(amount);
        return accountDTO;
    }

    void transferMoney(AccountDTO origin, AccountDTO destination, Double amount) throws Exception {
        NewAccountCreditTransactionDTO newAccountCreditTransactionDTO = new NewAccountCreditTransactionDTO();
        newAccountCreditTransactionDTO.setAmount(amount);

        NewAccountDebitTransactionDTO debitTransactionDTO = new NewAccountDebitTransactionDTO();
        debitTransactionDTO.setAmount(amount);
        debitTransactionDTO.setCard(origin.getCards().stream()
                .filter(c -> c.getType().equals(CardTypeEnum.DEBIT)).findFirst().get());

        var withdrawDtoJson = objectMapper.writeValueAsString(debitTransactionDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(withdrawDtoJson)
                .header("account_number", origin.getNumber())
                .header("destination_account_number", destination.getNumber())
        );
    }

    Double getAllAccountsBalanceSum() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/accounts/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        AccountsBalanceDTO response = objectMapper
                .readValue(result.getResponse().getContentAsString(), AccountsBalanceDTO.class);

        return response.getAccountsBalance().stream().map(AccountBalanceDTO::getBalance)
                .reduce(0.0, java.lang.Double::sum);
    }
}
