package app.inv.controller;

import app.inv.dto.LoginRequest;
import app.inv.dto.RegisterRequest;
import app.inv.entity.User;
import app.inv.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import app.inv.config.TestConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_success() throws Exception {
        User user = new User();
        user.setUsername("alice");
        user.setEmail("a@a.com");
        when(authService.register(any(), any(), any())).thenReturn(user);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setEmail("a@a.com");
        req.setPassword("pwd123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    void login_success() throws Exception {
        User user = new User();
        user.setUsername("alice");
        when(authService.authenticate("alice", "pwd")).thenReturn(java.util.Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("pwd");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    // RED阶段：编写失败测试
    @Test
    void register_shouldFailWhenUsernameAlreadyExists() throws Exception {
        // Given
        when(authService.register(any(), any(), any()))
                .thenThrow(new RuntimeException("用户名已存在"));

        RegisterRequest req = new RegisterRequest();
        req.setUsername("existinguser");
        req.setEmail("test@example.com");
        req.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void register_shouldFailWhenEmailAlreadyExists() throws Exception {
        // Given
        when(authService.register(any(), any(), any()))
                .thenThrow(new RuntimeException("邮箱已存在"));

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("existing@example.com");
        req.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱已存在"));
    }

    @Test
    void login_shouldFailWhenCredentialsInvalid() throws Exception {
        // Given
        when(authService.authenticate(any(), any()))
                .thenReturn(java.util.Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setUsername("invaliduser");
        req.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void register_shouldFailWhenValidationFails() throws Exception {
        // Given - 空的请求体
        RegisterRequest req = new RegisterRequest();
        req.setUsername(""); // 空用户名
        req.setEmail("invalid-email"); // 无效邮箱
        req.setPassword("123"); // 密码太短

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldFailWhenValidationFails() throws Exception {
        // Given - 空的请求体
        LoginRequest req = new LoginRequest();
        req.setUsername(""); // 空用户名
        req.setPassword(""); // 空密码

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentUser_shouldReturnUserInfo() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/me"))
                .andExpect(status().isMethodNotAllowed()); // GET方法，不是POST
    }

    @Test
    void getCurrentUser_shouldReturnUserInfoWithGetMethod() throws Exception {
        // When & Then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("test_user"));
    }
}


