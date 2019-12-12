package de.seprojekt.se2019.g4.mimir.security;

import org.springframework.LdapDataEntry;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Collection;

/**
 * This class be initiated by {@link WebSecurityConfig} and will create a custom {@link UserDetails} objects containing
 * additional user information (like surname, mail) and the default {@link LdapUserDetails}.
 * Taken from:
 * https://stackoverflow.com/a/30983147
 * https://stackoverflow.com/q/37728277
 */
@Configuration
public class OwnUserDetailsContextMapper extends LdapUserDetailsMapper implements UserDetailsContextMapper {

    /**
     * If the user can be authenticated and Spring Security wants to create a userDetails objects with some
     * LDAP attribute, this class intercepts and return our custom {@link OwnUserDetails} containing more
     * LDAP attributes and the {@link LdapUserDetails} object itself.
     * return
     *
     * @param ctx
     * @param username
     * @param authorities
     * @return
     */
    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        LdapUserDetails userDetails = (LdapUserDetails) super.mapUserFromContext(ctx, username, authorities);
        OwnUserDetails.LdapAttributes ldapAttributes = loadLdapAttributes(ctx);
        return new OwnUserDetails(userDetails, ldapAttributes);
    }

    /**
     * Load the additional LDAP information like surname or mail.
     *
     * @param entry
     * @return
     */
    private OwnUserDetails.LdapAttributes loadLdapAttributes(LdapDataEntry entry) {
        OwnUserDetails.LdapAttributes ldapAttributes = new OwnUserDetails.LdapAttributes();
        ldapAttributes.setSurname(getAttribute(entry, "sn"));
        ldapAttributes.setForename(getAttribute(entry, "givenName"));
        ldapAttributes.setCommonName(getAttribute(entry, "cn"));
        return ldapAttributes;
    }

    /**
     * Return attribute value if the attribute exists. If not, return empty string.
     *
     * @param ldapDataEntry
     * @param name
     * @return
     */
    private String getAttribute(LdapDataEntry ldapDataEntry, String name) {
        if (ldapDataEntry.attributeExists(name)) {
            return ldapDataEntry.getStringAttribute(name);
        }
        return "";
    }
}
