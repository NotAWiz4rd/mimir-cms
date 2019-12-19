package de.seprojekt.se2019.g4.mimir.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;;

import de.seprojekt.se2019.g4.mimir.security.user.User;

public class JwtPrincipal implements UserDetails {

  public static final String shareLinkUserName = "anonymous";

  private Optional<User> user;
  private Long sharedEntityId;
  private String sharedEntityType;

  public JwtPrincipal(Long sharedEntityId, String sharedEntityType) {
    this.user = Optional.empty();
    this.sharedEntityId = sharedEntityId;
    this.sharedEntityType = sharedEntityType;
  }

  public JwtPrincipal(User user) {
    this.user = Optional.of(user);
  }

  public Boolean isAnonymous() {
    return user.isEmpty();
  }

  public Long getSharedEntityId() {
    return sharedEntityId;
  }

  public String getSharedEntityType() {
    return sharedEntityType;
  }

  public User getUser() {
    return user.get();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.emptyList();
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return user.map(user -> user.getName()).orElse(shareLinkUserName);
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
