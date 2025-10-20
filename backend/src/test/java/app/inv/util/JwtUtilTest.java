package app.inv.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-should-be-long-enough-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600L);
    }

    @Test
    void generate_and_validate_token() {
        String username = "test_user";
        String token = jwtUtil.generateToken(username);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);
        assertThat(jwtUtil.validateToken(token, username)).isTrue();
    }
}


