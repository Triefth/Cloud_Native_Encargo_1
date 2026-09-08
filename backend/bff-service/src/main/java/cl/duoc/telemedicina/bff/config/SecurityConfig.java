package cl.duoc.telemedicina.bff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.cors.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://login.microsoftonline.com/common/v2.0}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token JWT inválido o ausente.\", \"status\": 401}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"No posee los permisos suficientes para acceder a este recurso.\", \"status\": 403}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/bff/auth/status", "/actuator/**", "/api/bff/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (issuerUri == null || issuerUri.isBlank() || issuerUri.contains("common")) {
            ObjectMapper mapper = new ObjectMapper();
            return token -> {
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        Map<String, Object> claims = mapper.readValue(payload, Map.class);
                        Instant now = Instant.now();
                        return new Jwt(
                            token,
                            now,
                            now.plusSeconds(86400),
                            Map.of("alg", "HS256"),
                            claims
                        );
                    }
                } catch (Exception e) {
                    // Fallthrough
                }
                throw new JwtException("Invalid token format");
            };
        }
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
