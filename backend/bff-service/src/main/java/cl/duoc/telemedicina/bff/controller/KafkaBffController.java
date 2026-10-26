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
public class KafkaBffController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.kafka.url:http://localhost:8888}")
    private String kafkaServiceUrl;

    // Enrutamiento directo según Guía 2: /api/kafka/**
    @RequestMapping(value = "/api/kafka/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> proxyKafkaRoot(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(kafkaServiceUrl + uri + queryString, request, body);
    }

    // Enrutamiento con prefijo BFF: /api/bff/kafka/**
    @RequestMapping(value = "/api/bff/kafka/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> proxyBffKafka(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI().replaceFirst("/api/bff/kafka", "/api/kafka");
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(kafkaServiceUrl + uri + queryString, request, body);
    }

    // Enrutamiento directo según Guía 1: /api/banco/**
    @RequestMapping(value = "/api/banco/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> proxyBancoRoot(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(kafkaServiceUrl + uri + queryString, request, body);
    }

    // Enrutamiento con prefijo BFF: /api/bff/banco/**
    @RequestMapping(value = "/api/bff/banco/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<?> proxyBffBanco(HttpServletRequest request, @RequestBody(required = false) Object body) {
        String uri = request.getRequestURI().replaceFirst("/api/bff/banco", "/api/banco");
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        return forward(kafkaServiceUrl + uri + queryString, request, body);
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
                    "message", "El microservicio kafka-service (" + kafkaServiceUrl + ") no está respondiendo. Verifica que kafka-service, kafka broker y zookeeper estén activos.",
                    "details", e.getMessage()
            ));
        }
    }
}
