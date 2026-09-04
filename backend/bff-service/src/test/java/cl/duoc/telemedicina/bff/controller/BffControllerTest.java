package cl.duoc.telemedicina.bff.controller;

import cl.duoc.telemedicina.bff.security.JwtTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BffControllerTest {

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BffController bffController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDevToken() {
        when(jwtTokenValidator.generateDevToken("test@medico.cl", "MEDICO")).thenReturn("dummy.jwt.token");

        ResponseEntity<?> response = bffController.getDevToken("test@medico.cl", "MEDICO");
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("dummy.jwt.token", body.get("token"));
        assertEquals("Bearer", body.get("token_type"));
    }

    @Test
    void testValidateTokenWithBlankTokenReturnsBadRequest() {
        ResponseEntity<?> response = bffController.validateToken(Map.of("token", ""));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testValidateTokenWithInvalidTokenReturnsUnauthorized() {
        when(jwtTokenValidator.validateToken("bad_token")).thenReturn(false);

        ResponseEntity<?> response = bffController.validateToken(Map.of("token", "bad_token"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
