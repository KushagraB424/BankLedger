package com.kushagragupta.bankledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kushagragupta.bankledger.dto.AuthResponse;
import com.kushagragupta.bankledger.dto.CreateAccountRequest;
import com.kushagragupta.bankledger.dto.DepositRequest;
import com.kushagragupta.bankledger.dto.LoginRequest;
import com.kushagragupta.bankledger.dto.RegisterRequest;
import com.kushagragupta.bankledger.dto.TransferRequest;
import com.kushagragupta.bankledger.dto.WithdrawRequest;
import com.kushagragupta.bankledger.entity.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BankLedgerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testEndToEndBankingFlow() throws Exception {
        // 1. Register User 1
        RegisterRequest register1 = new RegisterRequest("Alice", "Smith", "alice@example.com", "password", "5550001111");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register1)))
                .andExpect(status().isCreated());
                
        LoginRequest login1 = new LoginRequest("alice@example.com", "password");
        MvcResult loginResult1 = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login1)))
                .andExpect(status().isOk())
                .andReturn();
        String token1 = objectMapper.readValue(loginResult1.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        // 2. Register User 2
        RegisterRequest register2 = new RegisterRequest("Bob", "Jones", "bob@example.com", "password", "5550002222");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register2)))
                .andExpect(status().isCreated());
                
        LoginRequest login2 = new LoginRequest("bob@example.com", "password");
        MvcResult loginResult2 = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login2)))
                .andExpect(status().isOk())
                .andReturn();
        String token2 = objectMapper.readValue(loginResult2.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        // 3. Create Account for User 1
        CreateAccountRequest createAccReq = new CreateAccountRequest(AccountType.CURRENT);
        MvcResult accResult1 = mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccReq)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String accountId1 = objectMapper.readTree(accResult1.getResponse().getContentAsString()).get("id").asText();

        // 4. Create Account for User 2
        MvcResult accResult2 = mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccReq)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String accountId2 = objectMapper.readTree(accResult2.getResponse().getContentAsString()).get("id").asText();

        // 5. Deposit to User 1
        DepositRequest depositReq = new DepositRequest(new BigDecimal("1000.00"), "Initial Deposit");
        mockMvc.perform(post("/api/v1/accounts/" + accountId1 + "/deposit")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1000.00));

        // 6. Withdraw from User 1
        WithdrawRequest withdrawReq = new WithdrawRequest(new BigDecimal("200.00"), "ATM");
        mockMvc.perform(post("/api/v1/accounts/" + accountId1 + "/withdraw")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.00));

        // 7. Transfer from User 1 to User 2
        TransferRequest transferReq = new TransferRequest(
                java.util.UUID.fromString(accountId1),
                java.util.UUID.fromString(accountId2),
                new BigDecimal("300.00"),
                "Rent"
        );
        mockMvc.perform(post("/api/v1/transfers")
                .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(300.00));

        // 8. Attempt Unauthorized Transfer (User 2 trying to transfer FROM User 1)
        mockMvc.perform(post("/api/v1/transfers")
                .header("Authorization", "Bearer " + token2) // User 2's token
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferReq))) // But transferring from User 1's account
                .andExpect(status().isForbidden());
    }
}
