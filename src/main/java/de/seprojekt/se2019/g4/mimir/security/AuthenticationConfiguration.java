package de.seprojekt.se2019.g4.mimir.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring will create a 'singleton' of this class and will map the values of the environment
 * variables like user.authentication.method or user.authentication.ldap.user-search-filter onto
 * this 'singleton'. The 'singleton' will be autowired to constructors, which are requesting the
 * 'singleton'. The name of a environment variables can be written in different ways
 * (https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html):
 * - kebab case (for .properties, .yml files): user.authentication.ldap.user-search-filter - camel
 * case: user.authentication.ldap.userSearchFilter - underscore notation (for .properties, .yml
 * files): user.authentication.ldap.user_search_filter - upper case format (for system environment
 * variables): USER_AUTHENTICATION_LDAP_USERSEARCHFILTER
 */
@Configuration
@ConfigurationProperties(prefix = "user.authentication")
public class AuthenticationConfiguration {

  //user.authentication.method
  private Method method;
  private Ldap ldap;

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public Ldap getLdap() {
    return ldap;
  }

  public void setLdap(Ldap ldap) {
    this.ldap = ldap;
  }

  public enum Method {
    LDIF_FILE, LDAP_SERVER
  }

  public static class Ldap {

    //user.authentication.ldap.user-search-filter
    private String userSearchFilter;
    //user.authentication.ldap.user-search-base
    private String userSearchBase;
    //user.authentication.ldap.url
    private String url;
    //user.authentication.ldap.port
    private Integer port;
    //user.authentication.ldap.username
    private String username;
    //user.authentication.ldap.password
    private String password;
    //user.authentication.ldap.ldif
    private String ldif;
    //user.authentication.ldap.root
    private String root;
    //user.authentication.ldap.password-attribute
    private String passwordAttribute;

    public String getUserSearchFilter() {
      return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
      this.userSearchFilter = userSearchFilter;
    }

    public String getUserSearchBase() {
      return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
      this.userSearchBase = userSearchBase;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getLdif() {
      return ldif;
    }

    public void setLdif(String ldif) {
      this.ldif = ldif;
    }

    public String getRoot() {
      return root;
    }

    public void setRoot(String root) {
      this.root = root;
    }

    public String getPasswordAttribute() {
      return passwordAttribute;
    }

    public void setPasswordAttribute(String passwordAttribute) {
      this.passwordAttribute = passwordAttribute;
    }

    public Integer getPort() {
      return port;
    }

    public void setPort(Integer port) {
      this.port = port;
    }
  }
}
