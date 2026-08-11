package com.coinly;

import com.coinly.business.book.dto.CreateBookRequest;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.transaction.dto.CreateTransactionRequest;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.user.dto.LoginRequest;
import com.coinly.business.user.dto.LoginResponse;
import com.coinly.business.user.dto.RegisterRequest;
import com.coinly.common.domain.CommonResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * V9: Coinly 核心流程集成测试。
 * 覆盖：注册 -> 登录 -> 创建账本 -> 创建交易 -> 查询交易列表。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Disabled("需要本地 MySQL 数据库，默认跳过避免 CI 失败")
public class CoinlyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testMainFlow() throws Exception {
        String username = "integration" + System.currentTimeMillis();

        // 1. 注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPassword("123456");

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        CommonResponse<LoginResponse> registerResp = parseResponse(registerResult, new TypeReference<>() {});
        assert registerResp.getCode() == 200;
        String token = registerResp.getData().getToken();

        // 2. 登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // 3. 创建账本
        CreateBookRequest bookRequest = new CreateBookRequest();
        bookRequest.setName("测试账本");
        bookRequest.setDescription("集成测试用");

        MvcResult bookResult = mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn();

        CommonResponse<BookEntity> bookResp = parseResponse(bookResult, new TypeReference<>() {});
        assert bookResp.getCode() == 200;
        Long bookId = bookResp.getData().getId();

        // 4. 创建交易
        CreateTransactionRequest txRequest = new CreateTransactionRequest();
        txRequest.setCategoryId(1L);
        txRequest.setType(0);
        txRequest.setAmount(new BigDecimal("50.50"));
        txRequest.setRemark("午餐");
        txRequest.setTransactionDate(LocalDate.now());

        MvcResult txResult = mockMvc.perform(post("/api/v1/books/{bookId}/transactions", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txRequest)))
                .andExpect(status().isOk())
                .andReturn();

        CommonResponse<TransactionEntity> txResp = parseResponse(txResult, new TypeReference<>() {});
        assert txResp.getCode() == 200;

        // 5. 查询交易列表
        mockMvc.perform(get("/api/v1/books/{bookId}/transactions", bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private <T> CommonResponse<T> parseResponse(MvcResult result, TypeReference<CommonResponse<T>> typeRef) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), typeRef);
    }
}
