package cl.duoc.telemedicina.bff.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class RabbitMQBffController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.notificaciones.url:http://localhost:8084}")
    private String notificacionesServiceUrl;

    // Enrutamiento directo según Guía 2: POST /log
    @PostMapping("/log")
    public ResponseEntity<?> proxyLogRoot(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forward(notificacionesServiceUrl + "/log", request, body);
    }

    @GetMapping("/log")
    public ResponseEntity<?> proxyLogRootGet(HttpServletRequest request) {
        return forward(notificacionesServiceUrl + "/log/status", request, null);
    }

    // Enrutamiento directo según Guía 1: /api/messages
    @RequestMapping(value = "/api/messages/**", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> proxyMessagesRoot(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(notificacionesServiceUrl + uri + queryString, request, body);
    }

    // Enrutamiento prefijado BFF: /api/bff/log
    @RequestMapping(value = "/api/bff/log/**", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> proxyBffLog(HttpServletRequest request, @RequestBody(required = false) Object body) {
        return forward(notificacionesServiceUrl + "/log", request, body);
    }

    // Enrutamiento Guía 3: /api/orders/**
    @RequestMapping(value = "/api/orders/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> proxyOrders(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(notificacionesServiceUrl + uri + queryString, request, body);
    }

    // Enrutamiento Guía 4: /api/rabbitmq/**
    @RequestMapping(value = "/api/rabbitmq/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> proxyRabbitAdmin(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(notificacionesServiceUrl + uri + queryString, request, body);
    }

    // Enrutamiento Guía 4: /api/listeners/**
    @RequestMapping(value = "/api/listeners/**", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> proxyListeners(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(notificacionesServiceUrl + uri + queryString, request, body);
    }

    private ResponseEntity<?> forward(String targetUrl, HttpServletRequest request, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", "Service Unavailable",
                    "message", "El microservicio notificaciones-service (" + notificacionesServiceUrl + ") no está respondiendo. Revisa que notificaciones-service y rabbitmq estén corriendo.",
                    "details", e.getMessage()
            ));
        }
    }
}
