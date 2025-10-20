package app.inv.controller;

import app.inv.dto.ApiResponse;
import app.inv.dto.LoginRequest;
import app.inv.dto.RegisterRequest;
import app.inv.entity.User;
import app.inv.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("注册成功", userData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = authService.authenticate(request.getUsername(), request.getPassword());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
            }
            
            User user = userOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("token", "jwt_token_placeholder"); // TODO: 实现JWT Token生成
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", userData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("登录失败"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        // TODO: 从JWT Token中获取当前用户
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", "user_id_placeholder");
        userData.put("username", "test_user");
        userData.put("email", "test@example.com");
        
        return ResponseEntity.ok(ApiResponse.success(userData));
    }
}
