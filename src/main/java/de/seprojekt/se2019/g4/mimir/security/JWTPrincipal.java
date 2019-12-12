package de.seprojekt.se2019.g4.mimir.security;

import java.security.Principal;

public class JWTPrincipal implements Principal {

  public static final String shareLinkUserName = "anonymous";

  private String name;
  private Long sharedEntityId;
  private String sharedEntityType;

  public JWTPrincipal(String name, Long sharedEntityId, String sharedEntityType) {
    this.name = name;
    this.sharedEntityId = sharedEntityId;
    this.sharedEntityType = sharedEntityType;
  }

  public JWTPrincipal(String name) {
    this.name = name;
    this.sharedEntityId = null;
    this.sharedEntityType = null;
  }

  public Boolean isAnonymous() {
    return name.equals(JWTPrincipal.shareLinkUserName)
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
