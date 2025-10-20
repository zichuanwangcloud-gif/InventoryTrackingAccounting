package app.inv.service;

import app.inv.entity.User;
import app.inv.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = authService.register("alice", "a@a.com", "pwd");

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("a@a.com");
        assertThat(user.getPasswordHash()).isEqualTo("ENC");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicate_username() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        assertThatThrownBy(() -> authService.register("alice", null, "pwd"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void authenticate_success() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("ENC");
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("pwd"), eq("ENC"))).thenReturn(true);

        Optional<User> result = authService.authenticate("alice", "pwd");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void authenticate_wrong_password() {
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("ENC");
        when(userRepository.findByUsernameOrEmail("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("pwd"), eq("ENC"))).thenReturn(false);

        Optional<User> result = authService.authenticate("alice", "pwd");
        assertThat(result).isEmpty();
    }
}


