package de.seprojekt.se2019.g4.mimir.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import de.seprojekt.se2019.g4.mimir.security.JwtPrincipal;
import de.seprojekt.se2019.g4.mimir.security.user.UserRepository;

@Configuration
public class JwtUserDetailsContextMapper extends LdapUserDetailsMapper {
    @Autowired
    private UserRepository userRepository;

    @Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username,
        Collection<? extends GrantedAuthority> authorities) {
        return userRepository.findByName(username)
            .map(JwtPrincipal::new)
            .get();
    }
}