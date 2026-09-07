package cl.duoc.telemedicina.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bff")
public class BffController {

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

    @Value("${services.usuarios.url:http://localhost:8086}")
    private String usuariosServiceUrl;

    @Value("${services.clinicas.url:http://localhost:8087}")
    private String clinicasServiceUrl;

    // --- Estado del BFF (ruta publica; los endpoints de negocio requieren JWT) ---

    @GetMapping("/auth/status")
    public ResponseEntity<?> authStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "authentication", "Microsoft Entra ID / Spring OAuth2 Resource Server"
        ));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> authMe(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "valid", authentication != null && authentication.isAuthenticated(),
                "principal", authentication == null ? "" : authentication.getName(),
                "authorities", authentication == null ? List.of() : authentication.getAuthorities()
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

    @RequestMapping(value = "/usuarios/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyUsuarios(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(usuariosServiceUrl, "/api/usuarios", request, body);
    }

    @RequestMapping(value = "/clinicas/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyClinicas(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forwardRequest(clinicasServiceUrl, "/api/clinicas", request, body);
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
