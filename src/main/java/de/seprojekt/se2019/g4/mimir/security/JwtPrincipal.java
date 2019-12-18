package de.seprojekt.se2019.g4.mimir.security;

import java.security.Principal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class JwtPrincipal implements Principal {

  public static final String shareLinkUserName = "anonymous";

  private String name;
  private Long sharedEntityId;
  private String sharedEntityType;

  public JwtPrincipal(String name, Long sharedEntityId, String sharedEntityType) {
    this.name = name;
    this.sharedEntityId = sharedEntityId;
    this.sharedEntityType = sharedEntityType;
  }

  public JwtPrincipal(String name) {
    this.name = name;
    this.sharedEntityId = null;
    this.sharedEntityType = null;
  }

  public static JwtPrincipal fromPrincipal(Principal principal) {
    return (JwtPrincipal) (((UsernamePasswordAuthenticationToken) principal).getPrincipal());
  }

  public Boolean isAnonymous() {
    return name.equals(JwtPrincipal.shareLinkUserName)
        && this.sharedEntityId != null
        && this.sharedEntityType != null;
  }

  public String getName() {
    return name;
  }

  public Long getSharedEntityId() {
    return sharedEntityId;
  }

  public String getSharedEntityType() {
    return sharedEntityType;
  }
}
