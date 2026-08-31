package cl.duoc.telemedicina.bff.controller;

import cl.duoc.telemedicina.bff.security.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bff")
public class BffController {

    @Autowired
    private JwtTokenValidator jwtTokenValidator;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.citas.url:http://localhost:8081}")
    private String citasServiceUrl;

    @Value("${services.consultas.url:http://localhost:8082}")
    private String consultasServiceUrl;

    @Value("${services.fichas.url:http://localhost:8083}")
    private String fichasServiceUrl;

    @Value("${services.notificaciones.url:http://localhost:8084}")
    private String notificacionesServiceUrl;

    @Value("${services.reportes.url:http://localhost:8085}")
    private String reportesServiceUrl;

    // --- Autenticación y Validación JWT (Rutas Públicas) ---

    @PostMapping("/auth/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Se requiere el token JWT"));
        }
        boolean isValid = jwtTokenValidator.validateToken(token);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "message", "Token inválido, expirado o con firma incorrecta"
            ));
        }

        Claims claims = jwtTokenValidator.getClaimsFromToken(token);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", jwtTokenValidator.getUsernameFromToken(token));
        response.put("issuer", claims.getIssuer());
        response.put("audience", claims.getAudience());
        response.put("expiration", claims.getExpiration());
        response.put("roles", jwtTokenValidator.getAuthoritiesFromToken(token));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/dev-token")
    public ResponseEntity<?> getDevToken(@RequestParam(defaultValue = "medico.rural@telemedicina.cl") String user,
                                        @RequestParam(defaultValue = "MEDICO") String role) {
        String token = jwtTokenValidator.generateDevToken(user, role);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "token_type", "Bearer",
                "user", user,
                "role", role
        ));
    }

    // --- Proxy seguro a Microservicios Desacoplados ---

    @RequestMapping(value = "/citas/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyCitas(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(citasServiceUrl, "/api/citas", request, body);
    }

    @RequestMapping(value = "/consultas/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyConsultas(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(consultasServiceUrl, "/api/consultas", request, body);
    }

    @RequestMapping(value = "/fichas/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyFichas(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(fichasServiceUrl, "/api/fichas", request, body);
    }

    @RequestMapping(value = "/notificaciones/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyNotificaciones(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(notificacionesServiceUrl, "/api/notificaciones", request, body);
    }

    @RequestMapping(value = "/reportes/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyReportes(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(reportesServiceUrl, "/api/reportes", request, body);
    }

    // --- Helper de Enrutamiento y Resiliencia ---

    private ResponseEntity<?> forwardRequest(String baseUrl, String targetPrefix, HttpServletRequest request, Object body) {
        String uri = request.getRequestURI();
        // Extraer la ruta posterior al prefijo /api/bff/[servicio]
        String subPath = uri.replaceAll("^/api/bff/[^/]+", "");
        String targetUrl = baseUrl + targetPrefix + subPath;
        if (request.getQueryString() != null) {
            targetUrl += "?" + request.getQueryString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            return restTemplate.exchange(targetUrl, method, entity, Object.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            // Manejo de resiliencia: Si el microservicio está caído, los demás siguen funcionando
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("error", "Service Unavailable");
            fallbackResponse.put("message", "El microservicio de destino en [" + baseUrl + "] no está disponible temporalmente.");
            fallbackResponse.put("status", 503);
            fallbackResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse);
        }
    }
}
