package de.seprojekt.se2019.g4.mimir.security;

import de.seprojekt.se2019.g4.mimir.security.AuthenticationConfiguration.Ldap;
import java.nio.charset.Charset;
import javax.naming.Name;
import org.springframework.ldap.core.AuthenticationSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * https://www.baeldung.com/spring-ldap
 */
@Component
public class LdapClient {

  private Ldap ldap;

  public LdapClient(AuthenticationConfiguration authenticationConfiguration) {
    this.ldap = authenticationConfiguration.getLdap();
  }

  /**
   * registers user in ldap server
   */
  public void registerLdapUser(final String username, final String password) {
    LdapContextSource ldapContextSource = new LdapContextSource();
    ldapContextSource.setUrl(ldap.getUrl() + ":" + ldap.getPort() + "/" + ldap.getRoot());

    if (ldap.getPassword() != null && ldap.getUsername() != null) {
      AuthenticationSource authenticationSource = new AuthenticationSource() {
        @Override
        public String getPrincipal() {
          return ldap.getUsername();
        }

        @Override
        public String getCredentials() {
          return ldap.getPassword();
        }
      };
      ldapContextSource.setAuthenticationSource(authenticationSource);
    } else {
      ldapContextSource.setAnonymousReadOnly(true);
    }

    ldapContextSource.afterPropertiesSet();

    LdapTemplate ldapTemplate = new LdapTemplate(ldapContextSource);

    Name dn = LdapNameBuilder
        .newInstance()
        .add("ou", "users")
        .add("uid", username)
        .build();
    DirContextAdapter context = new DirContextAdapter(dn);

    context.setAttributeValue("uid", username);
    context.setAttributeValue("cn", username);
    context.setAttributeValues("objectClass",
        new String[]{"inetOrgPerson", "person", "top"});
    context.setAttributeValue("sn", username);
    context.setAttributeValue("userPassword", new BCryptPasswordEncoder()
        .encode((new String(password.getBytes(), Charset.forName("UTF-8")))));

    ldapTemplate.bind(context);
  }

}
