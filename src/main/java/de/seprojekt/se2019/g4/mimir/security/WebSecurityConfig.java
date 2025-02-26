package de.seprojekt.se2019.g4.mimir.security;

import static de.seprojekt.se2019.g4.mimir.security.AuthenticationConfiguration.Ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * This class will configure the security and ldap security aspect of the application
 */
@Configuration
@EnableConfigurationProperties(AuthenticationConfiguration.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final static Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);
  private AuthenticationConfiguration config;

  /**
   * The parameters will be autowired by Spring.
   */
  public WebSecurityConfig(AuthenticationConfiguration authenticationConfiguration) {
    this.config = authenticationConfiguration;
  }

  /**
   * Do the HTTP security configuration (e.g. which page is visible for anonymous user etc.)
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/register/mail")
        .permitAll()
        .antMatchers(HttpMethod.POST, "/register/confirm")
        .permitAll()
        .and()
        .authorizeRequests()
        .anyRequest()
        .authenticated()
        .and()
        // https://stackoverflow.com/questions/48107157/autowired-is-null-in-usernamepasswordauthenticationfilter-spring-boot
        .addFilter(new JwtAuthenticationFilter(authenticationManager(), getApplicationContext()))
        .addFilter(new JwtAuthorizationFilter(authenticationManager(), getApplicationContext()))
        .cors().and()
        .csrf().disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
  }

  /**
   * Do the general authentication configuration. Either authentication against a real LDAP server
   * or a local LDAP server which loads its data from a LDIF file.
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    LOGGER.info("Current authentication method: {}", config.getMethod());
    switch (config.getMethod()) {
      case LDIF_FILE:
        configureLdapFromFile(auth);
        break;
      case LDAP_SERVER:
        configureLdapFromServer(auth);
        break;
      default:
        throw new RuntimeException("The configuration user.authentication.method must be set.");
    }
  }

  /**
   * Do the LDAP configuration for authenticating against a real, productive LDAP server.
   */
  private void configureLdapFromServer(AuthenticationManagerBuilder auth) throws Exception {
    Ldap ldapConfig = config.getLdap();
    configureLdap(auth)
        .url(ldapConfig.getUrl() + ":" + ldapConfig.getPort() + "/" + ldapConfig.getRoot())
        .managerDn(ldapConfig.getUsername())
        .managerPassword(ldapConfig.getPassword())
        .and()
        .passwordCompare()
        .passwordEncoder(new BCryptPasswordEncoder())
        .passwordAttribute(ldapConfig.getPasswordAttribute());
  }

  /**
   * Do the LDAP configuration for authenticating against a local created LDAP server which loads
   * its data from a local LDIF (LDAP Data Interchange Format) file.
   */
  private void configureLdapFromFile(AuthenticationManagerBuilder auth) throws Exception {
    Ldap ldapConfig = config.getLdap();
    configureLdap(auth)
        .ldif(ldapConfig.getLdif())
        .root(ldapConfig.getRoot())
        .port(ldapConfig.getPort())
        .and()
        .passwordCompare()
        .passwordEncoder(new BCryptPasswordEncoder())
        .passwordAttribute(ldapConfig.getPasswordAttribute());
  }

  /**
   * Do the basic ldap configuration.
   */
  private LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder>.ContextSourceBuilder configureLdap(
      AuthenticationManagerBuilder auth) throws Exception {
    Ldap ldapConfig = config.getLdap();
    return auth.ldapAuthentication()
        .userSearchFilter(ldapConfig.getUserSearchFilter())
        .userSearchBase(ldapConfig.getUserSearchBase())
        .contextSource();
  }
}


