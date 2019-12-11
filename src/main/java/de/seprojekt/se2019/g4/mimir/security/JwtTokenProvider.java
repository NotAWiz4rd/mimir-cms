package de.seprojekt.se2019.g4.mimir.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateToken(Authentication auth) {
        var user = ((OwnUserDetails) auth.getPrincipal());
        return Jwts.builder()
                .signWith(key())
                .setSubject(user.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .compact();
    }

    public String generateShareToken(Long sharedEntityId, Integer expirationMs)
        throws JsonProcessingException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", OwnPrincipal.shareLinkUserName);
        claims.put("id", String.valueOf(sharedEntityId));

        String token = this.generateShareTokenWitchClaims(claims, expirationMs);
        ObjectMapper om = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        map.put("token", token);

        return om.writeValueAsString(map);
    }

    private String generateShareTokenWitchClaims(Map<String, Object> claims, Integer expirationMs) {
        if(expirationMs == null) {
            expirationMs = jwtExpirationMs;
        }

        return Jwts.builder()
            .signWith(key())
            .setClaims(claims)
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key())
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException exception) {
            log.warn("Request to parse expired JWT : {} failed : {}", token, exception.getMessage());
        } catch (UnsupportedJwtException exception) {
            log.warn("Request to parse unsupported JWT : {} failed : {}", token, exception.getMessage());
        } catch (MalformedJwtException exception) {
            log.warn("Request to parse invalid JWT : {} failed : {}", token, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            log.warn("Request to parse empty or null JWT : {} failed : {}", token, exception.getMessage());
        }

        return false;
    }

    public String getPayload(String token, String key) {
        return (String) Jwts.parser()
            .setSigningKey(key())
            .parseClaimsJws(token)
            .getBody()
            .get(key);
    }

}
