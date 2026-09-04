package cl.duoc.telemedicina.bff.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenValidatorTest {

    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void setUp() {
        jwtTokenValidator = new JwtTokenValidator();
        ReflectionTestUtils.setField(jwtTokenValidator, "expectedIssuer", "https://login.microsoftonline.com/common/v2.0");
        ReflectionTestUtils.setField(jwtTokenValidator, "expectedAudience", "api://telemedicina-rural-api");
        ReflectionTestUtils.setField(jwtTokenValidator, "secretKey", "SuperSecretKeyForDevelopmentTestingTelemedicinaRural2025DUOCUC!");
        ReflectionTestUtils.setField(jwtTokenValidator, "devModeEnabled", true);
    }

    @Test
    void testGenerateAndValidateDevToken() {
        String token = jwtTokenValidator.generateDevToken("medico@rural.cl", "MEDICO");
        assertNotNull(token);
        assertTrue(jwtTokenValidator.validateToken(token));

        String username = jwtTokenValidator.getUsernameFromToken(token);
        assertEquals("medico@rural.cl", username);

        List<SimpleGrantedAuthority> authorities = jwtTokenValidator.getAuthoritiesFromToken(token);
        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MEDICO")));
    }

    @Test
    void testInvalidTokenReturnsFalse() {
        assertFalse(jwtTokenValidator.validateToken("invalid.token.string"));
    }
}
