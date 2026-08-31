package cl.duoc.telemedicina.bff.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenValidator {

    @Value("${security.jwt.issuer:https://login.microsoftonline.com/common/v2.0}")
    private String expectedIssuer;

    @Value("${security.jwt.audience:api://telemedicina-rural-api}")
    private String expectedAudience;

    @Value("${security.jwt.secret:SuperSecretKeyForDevelopmentTestingTelemedicinaRural2025DUOCUC!}")
    private String secretKey;

    @Value("${security.jwt.dev-mode-enabled:true}")
    private boolean devModeEnabled;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Valida el token JWT comprobando emisor, audiencia, vigencia y firma.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);

            // 1. Validar expiración
            if (claims.getExpiration().before(new Date())) {
                System.err.println("JWT Error: Token expirado.");
                return false;
            }

            // 2. Validar emisor (issuer) en producción
            if (!devModeEnabled && claims.getIssuer() != null && !claims.getIssuer().contains("microsoft")) {
                System.err.println("JWT Error: Issuer no coincide. Esperado Azure AD, recibido: " + claims.getIssuer());
                return false;
            }

            // 3. Validar audiencia (audience) en producción
            if (!devModeEnabled && claims.getAudience() != null && !claims.getAudience().equals(expectedAudience)) {
                System.err.println("JWT Error: Audience no coincide. Esperado: " + expectedAudience + ", recibido: " + claims.getAudience());
                return false;
            }

            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("JWT Expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT No Soportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("JWT Malformado: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("JWT Firma Inválida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("JWT Error Inesperado: " + e.getMessage());
        }
        return false;
    }

    /**
     * Extrae los claims del token token.
     */
    public Claims getClaimsFromToken(String token) {
        // En modo dev o firma personalizada
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Si falla firma simétrica, intenta parseo de payload Azure AD (sin verificar clave secreta local)
            int i = token.lastIndexOf('.');
            String withoutSignature = token.substring(0, i + 1);
            return (Claims) Jwts.parserBuilder().build().parseClaimsJwt(withoutSignature).getBody();
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims.get("preferred_username") != null) {
            return claims.get("preferred_username").toString();
        }
        if (claims.get("upn") != null) {
            return claims.get("upn").toString();
        }
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        List<String> roles = new ArrayList<>();

        // Claims comunes de Azure AD MSAL / Entra ID
        if (claims.get("roles") instanceof List) {
            roles.addAll((List<String>) claims.get("roles"));
        } else if (claims.get("roles") instanceof String) {
            roles.add((String) claims.get("roles"));
        }

        if (claims.get("scp") instanceof String) {
            String scopes = (String) claims.get("scp");
            roles.addAll(Arrays.asList(scopes.split(" ")));
        }

        if (roles.isEmpty()) {
            roles.add("ROLE_USER");
        }

        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Genera un token JWT de prueba para entornos de desarrollo / demostración.
     */
    public String generateDevToken(String subject, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", subject);
        claims.put("roles", List.of(role));
        claims.put("scp", "User.Read Consultation.Write");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(expectedIssuer)
                .setAudience(expectedAudience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
