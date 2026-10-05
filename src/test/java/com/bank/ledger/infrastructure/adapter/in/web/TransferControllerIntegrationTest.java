package com.bank.ledger.infrastructure.adapter.in.web;

import com.bank.ledger.AbstractIntegrationTest;
import com.bank.ledger.infrastructure.adapter.in.web.dto.TransferRequest;
import com.bank.ledger.infrastructure.adapter.out.persistence.entity.AccountJpaEntity;
import com.bank.ledger.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.bank.ledger.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransferControllerIntegrationTest extends AbstractIntegrationTest
{
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SpringDataAccountRepository accountRepository;

    @Autowired
    private SpringDataTransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private UUID sourceAccountId;
    private UUID targetAccountId;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        sourceAccountId = UUID.randomUUID();
        targetAccountId = UUID.randomUUID();

        // Seed Initial Accounts
        accountRepository.save(new AccountJpaEntity(sourceAccountId, "ACC-001", new BigDecimal("1000.00"), "USD", 0L));
        accountRepository.save(new AccountJpaEntity(targetAccountId, "ACC-002", new BigDecimal("500.00"), "USD", 1L));
    }

    @Test
    @DisplayName("Should execute money transfer successfully")
    void shouldExecuteTransferSuccessfully() throws Exception
    {
        TransferRequest request = new TransferRequest(
                "REF-TX-100",
                sourceAccountId,
                targetAccountId,
                new BigDecimal("250.00"),
                "USD"
        );

        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceId").value("REF-TX-100"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Validate DB Account Balances
        AccountJpaEntity updatedSource = accountRepository.findById(sourceAccountId).orElseThrow();
        AccountJpaEntity updatedTarget = accountRepository.findById(targetAccountId).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("750.00");
        assertThat(updatedTarget.getBalance()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("Should return same response on duplicate idempotency key request")
    void shouldHandleDuplicateIdempotencyKey() throws Exception
    {
        TransferRequest request = new TransferRequest(
                "REF-IDEMPOTENT-01",
                sourceAccountId,
                targetAccountId,
                new BigDecimal("100.00"),
                "USD"
        );

        // First Transfer
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate Transfer Request
        mockMvc.perform(post("/api/v1/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceId").value("REF-IDEMPOTENT-01"));

        // Balance should only be debited once
        AccountJpaEntity updatedSource = accountRepository.findById(sourceAccountId).orElseThrow();
        assertThat(updatedSource.getBalance()).isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("Should maintain balance consistency under high concurrent requests using Virtual Threads")
    void shouldMaintainConsistencyUnderHighConcurrency() throws InterruptedException
    {
        int threadCount = 50;
        BigDecimal transferAmount = new BigDecimal("10.00"); // 50 * 10 = 500 debited total

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor())
        {
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++)
            {
                final String refId = "REF-CONCURRENT-" + i;
                executor.submit(() -> {
                    try
                    {
                        TransferRequest request = new TransferRequest(
                                refId,
                                sourceAccountId,
                                targetAccountId,
                                transferAmount,
                                "USD"
                        );
                        mockMvc.perform(post("/api/v1/transfers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)));
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        // Verify Final Balances
        AccountJpaEntity finalSource = accountRepository.findById(sourceAccountId).orElseThrow();
        AccountJpaEntity finalTarget = accountRepository.findById(targetAccountId).orElseThrow();

        assertThat(finalSource.getBalance()).isEqualByComparingTo("500.00");
        assertThat(finalTarget.getBalance()).isEqualByComparingTo("1000.00");
    }
}