package de.seprojekt.se2019.g4.mimir.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.seprojekt.se2019.g4.mimir.security.user.User;
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
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

  @Value("${app.jwtSecret}")
  private String jwtSecret;

  @Value("${app.jwtExpirationMs}")
  private Long jwtExpirationMs;

  @Value("${app.jwtDownloadExpirationMs}")
  private Long jwtDownloadExpirationMs;

  @Value("${app.jwtRegistrationExpirationMs}")
  private Long jwtRegistrationExpirationMs;

  public String generateToken(Authentication auth) {
    var user = ((LdapUserDetails) auth.getPrincipal());
    return Jwts.builder()
        .signWith(key())
        .setSubject(user.getUsername())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .compact();
  }

  public String generateDownloadToken(Long sharedEntityId, String sharedEntityType)
      throws JsonProcessingException {
    return this.generateShareToken(sharedEntityId, sharedEntityType, jwtDownloadExpirationMs);
  }

  public String generateShareToken(Long sharedEntityId, String sharedEntityType,
      Long expirationMs)
      throws JsonProcessingException {
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", JwtPrincipal.shareLinkUserName);
    claims.put("id", String.valueOf(sharedEntityId));
    claims.put("type", sharedEntityType);

    String token = this.generateShareTokenWithClaims(claims, expirationMs);
    ObjectMapper om = new ObjectMapper();
    Map<String, String> map = new HashMap<>();
    map.put("token", token);

    return om.writeValueAsString(map);
  }

  public String generateRegistrationToken(String mail) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", mail);
    claims.put("type", User.REGISTRATION_IDENTIFIER);

    return this.generateShareTokenWithClaims(claims, jwtRegistrationExpirationMs);
  }

  private String generateShareTokenWithClaims(Map<String, Object> claims, Long expirationMs) {
    if (expirationMs == null) {
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
      log.warn("Request to parse empty or null JWT : {} failed : {}", token,
          exception.getMessage());
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

  public Date getExpiration(String token) {
    return Jwts.parser()
        .setSigningKey(key())
        .parseClaimsJws(token)
        .getBody()
        .getExpiration();
  }

}
