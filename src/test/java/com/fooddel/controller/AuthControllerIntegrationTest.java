package com.fooddel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddel.dto.request.LoginRequest;
import com.fooddel.dto.request.UserRequest;
import com.fooddel.dto.response.AuthResponse;
import com.fooddel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 各テスト後にデータベース変更をロールバック
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // 各テスト後にアプリケーションコンテキストをリフレッシュ
@ActiveProfiles("test") // テストプロファイルをアクティブにする
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private UserRequest testUserRequest;
    private LoginRequest testLoginRequest;

    @BeforeEach
    void setUp() {
        testUserRequest = new UserRequest();
        testUserRequest.setName("Test User");
        testUserRequest.setEmail("test@example.com");
        testUserRequest.setPassword("password123");
        testUserRequest.setPhone("1234567890");

        testLoginRequest = new LoginRequest();
        testLoginRequest.setEmail("test@example.com");
        testLoginRequest.setPassword("password123");
    }

    @Test
    void testUserRegistrationSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ユーザー登録が成功しました。"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void testUserRegistrationFailure_EmailAlreadyExists() throws Exception {
        // 最初にユーザーを登録
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated());

        // 同じメールアドレスで再度登録を試みる
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("指定されたメールアドレスは既に使用されています。"));
    }

    @Test
    void testUserLoginSuccess() throws Exception {
        // ユーザーを登録
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated());

        // ログインを試みる
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ログインに成功しました。"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void testUserLoginFailure_BadCredentials() throws Exception {
        // ユーザーを登録
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated());

        // 間違ったパスワードでログインを試みる
        LoginRequest wrongPasswordLoginRequest = new LoginRequest();
        wrongPasswordLoginRequest.setEmail("test@example.com");
        wrongPasswordLoginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bad credentials")); // Spring Securityのデフォルトメッセージ
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        // ユーザーを登録
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated());

        // ログインしてリフレッシュトークンを取得
        String loginResponseContent = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponseContent, AuthResponse.class);
        String refreshToken = authResponse.getRefreshToken();

        // リフレッシュトークンを使用して新しいアクセストークンを取得
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("トークンが正常に更新されました。"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken)); // リフレッシュトークンは変わらない
    }

    @Test
    void testRefreshTokenFailure_InvalidToken() throws Exception {
        // 無効なリフレッシュトークンでリフレッシュを試みる
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"invalid.refresh.token\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無効なリフレッシュトークンです。"));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        // ユーザーを登録
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated());

        // ログインしてアクセストークンを取得
        String loginResponseContent = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(loginResponseContent, AuthResponse.class);
        String accessToken = authResponse.getAccessToken();

        // ログアウトを試みる
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ログアウトしました。"));

        // ログアウトしたトークンで保護されたリソースにアクセスを試みる (例: /api/users/profile)
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized()); // トークンが無効化されているため401
    }
}
