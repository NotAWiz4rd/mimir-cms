

package de.seprojekt.se2019.g4.mimir.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import static de.seprojekt.se2019.g4.mimir.web.AuthenticationConfiguration.Ldap;

/**
 * This class will configure the web and ldap security aspect of the application
 */
//@Configuration
//@EnableConfigurationProperties(AuthenticationConfiguration.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);
    private AuthenticationConfiguration config;
    private String applicationBaseUrl;

    /**
     * The parameters will be autowired by Spring.
     *
     * @param authenticationConfiguration
     * @param applicationBaseUrl
     */
    public WebSecurityConfig(AuthenticationConfiguration authenticationConfiguration, @Value("${application.base.url}") String applicationBaseUrl) {
        this.config = authenticationConfiguration;
        this.applicationBaseUrl = applicationBaseUrl;
    }

    /**
     * Do the HTTP security configuration (e.g. which page is visible for anonymous user etc.)
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //permit/forbid access to these urls
        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/favicon.png").permitAll()
                .antMatchers("/favicon.ico").permitAll()
                .anyRequest().authenticated();

        //allow iframes
        http.headers().frameOptions().sameOrigin();
    }

    /**
     * Do the general authentication configuration. Either authentication against a real LDAP server or a
     * local LDAP server which loads its data from a LDIF file.
     *
     * @param auth
     * @throws Exception
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
     * @return
     */
    @Bean
    public UserDetailsContextMapper userDetailsContextMapper() {
        return new OwnUserDetailsContextMapper();
    }

    /**
     * Do the LDAP configuration for authenticating against a real, productive LDAP server.
     *
     * @param auth
     * @throws Exception
     */
    private void configureLdapFromServer(AuthenticationManagerBuilder auth) throws Exception {
        Ldap ldapConfig = config.getLdap();
        configureLdap(auth)
                .url(ldapConfig.getUrl())
                .managerDn(ldapConfig.getUsername())
                .managerPassword(ldapConfig.getPassword());
    }

    /**
     * Do the LDAP configuration for authenticating against a local created LDAP server which loads its data from a local
     * LDIF (LDAP Data Interchange Format) file.
     *
     * @param auth
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    private void configureLdapFromFile(AuthenticationManagerBuilder auth) throws Exception {
        Ldap ldapConfig = config.getLdap();
        configureLdap(auth)
                .ldif(ldapConfig.getLdif())
                .root(ldapConfig.getRoot())
                .and()
                .passwordCompare()
                /*
                * we need LdapShaPasswordEncoder for using LDIF -
                * LdapShaPasswordEncoder is deprecated because digest based password encoding is not considered secure.
                * There are no plans to remove this support.
                */
                .passwordEncoder(new LdapShaPasswordEncoder())
                .passwordAttribute(ldapConfig.getPasswordAttribute());

    }

    /**
     * Do the basic ldap configuration.
     *
     * @param auth
     * @return
     * @throws Exception
     */
    private LdapAuthenticationProviderConfigurer<AuthenticationManagerBuilder>.ContextSourceBuilder configureLdap(AuthenticationManagerBuilder auth) throws Exception {
        Ldap ldapConfig = config.getLdap();
        return auth.ldapAuthentication()
                .userSearchFilter(ldapConfig.getUserSearchFilter())
                .userSearchBase(ldapConfig.getUserSearchBase())
                .groupSearchBase(ldapConfig.getGroupSearchBase())
                .groupSearchFilter(ldapConfig.getGroupSearchFilter())
                .userDetailsContextMapper(userDetailsContextMapper())
                .contextSource();
    }
}


