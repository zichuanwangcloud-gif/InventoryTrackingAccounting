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
}


