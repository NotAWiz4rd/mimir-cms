package de.seprojekt.se2019.g4.mimir.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

/**
 * This class acts like a {@link LdapUserDetails} but holds some additional LDAP attributes.
 */
public class OwnUserDetails implements LdapUserDetails {

  private LdapUserDetails ldapUserDetails;
  private LdapAttributes ldapAttributes;

  OwnUserDetails(LdapUserDetails ldapUserDetails, LdapAttributes ldapAttributes) {
    this.ldapUserDetails = ldapUserDetails;
    this.ldapAttributes = ldapAttributes;
  }

  @Override
  public String getDn() {
    return ldapUserDetails.getDn();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return ldapUserDetails.getAuthorities();
  }

  @Override
  public String getPassword() {
    return ldapUserDetails.getPassword();
  }

  @Override
  public String getUsername() {
    return ldapUserDetails.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return ldapUserDetails.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return ldapUserDetails.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return ldapUserDetails.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return ldapUserDetails.isEnabled();
  }

  @Override
  public void eraseCredentials() {
    ldapUserDetails.eraseCredentials();
  }

  public String getForename() {
    return ldapAttributes.forename;
  }

  public String getSurname() {
    return ldapAttributes.surname;
  }

  public String getCommonName() {
    return ldapAttributes.commonName;
  }

  @Override
  public String toString() {
    return "OwnUserDetails{" +
        "dn=" + getDn() +
        ", authorities=" + getAuthorities() +
        ", password=" + getPassword() +
        ", username=" + getUsername() +
        ", isAccountNonExpired=" + isAccountNonExpired() +
        ", isAccountNonLocked=" + isAccountNonLocked() +
        ", isCredentialsNonExpired=" + isCredentialsNonExpired() +
        ", isEnabled=" + isEnabled() +
        ", forename=" + getForename() +
        ", commonName=" + getCommonName() +
        '}';
  }

  static class LdapAttributes {

    private String forename;
    private String surname;
    private String commonName;

    void setForename(String forename) {
      this.forename = forename;
    }

    void setSurname(String surname) {
      this.surname = surname;
    }

    void setCommonName(String commonName) {
      this.commonName = commonName;
    }

  }
}
