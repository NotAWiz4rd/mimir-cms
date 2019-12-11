package de.seprojekt.se2019.g4.mimir.security;

import java.security.Principal;

public class OwnPrincipal implements Principal {

  public static final String shareLinkUserName = "anonymous";

  private String name;
  private Long sharedEntityId;

  public OwnPrincipal(String name, Long sharedEntityId) {
    this.name = name;
    this.sharedEntityId = sharedEntityId;
  }

  public Boolean isAnonymous() {
    return name.equals(OwnPrincipal.shareLinkUserName) || this.sharedEntityId != null;
  }

  public String getName() {
    return name;
  }

  public Long getSharedEntityId() {
    return sharedEntityId;
  }
}
