package com.cydeo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts Keycloak realm/resource roles from the OIDC token claims and converts them
 * to Spring Security GrantedAuthority objects (prefixed with "ROLE_").
 *
 * Priority:
 *  1. resource_access.ticketing-app.roles  ← matches backend use-resource-role-mappings=true
 *  2. realm_access.roles                   ← fallback if resource roles not present
 *
 * Result: role "Admin" → authority "ROLE_Admin"
 *         so hasRole("Admin") / sec:authorize="hasRole('Admin')" work as expected.
 */
@Slf4j
@Component
public class KeycloakOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final String CLIENT_ID      = "ticketing-app";
    private static final String RESOURCE_ACCESS = "resource_access";
    private static final String REALM_ACCESS    = "realm_access";
    private static final String ROLES           = "roles";

    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.addAll(toRoleAuthorities(oidcUser.getClaims()));

        log.debug("Keycloak authorities for {}: {}", oidcUser.getPreferredUsername(), authorities);

        // preferred_username is the name attribute declared in application.yml
        if (oidcUser.getUserInfo() != null) {
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(),
                    oidcUser.getUserInfo(), "preferred_username");
        }
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), "preferred_username");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Collection<SimpleGrantedAuthority> toRoleAuthorities(Map<String, Object> claims) {
        List<String> roles = extractResourceRoles(claims);
        if (roles.isEmpty()) {
            roles = extractRealmRoles(claims);   // fallback
        }
        if (roles.isEmpty()) {
            log.warn("No application roles found in OIDC claims. "
                    + "Verify that the 'roles' scope is requested and Keycloak mappers are configured.");
        }
        List<SimpleGrantedAuthority> result = new ArrayList<>();
        for (String role : roles) {
            result.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return result;
    }

    /**
     * Reads resource_access.ticketing-app.roles — the primary source when
     * Keycloak is configured with use-resource-role-mappings=true.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractResourceRoles(Map<String, Object> claims) {
        try {
            Object ra = claims.get(RESOURCE_ACCESS);
            if (!(ra instanceof Map)) return Collections.emptyList();
            Object clientObj = ((Map<?, ?>) ra).get(CLIENT_ID);
            if (!(clientObj instanceof Map)) return Collections.emptyList();
            Object rolesObj = ((Map<?, ?>) clientObj).get(ROLES);
            if (rolesObj instanceof List) return (List<String>) rolesObj;
        } catch (Exception e) {
            log.debug("Could not extract resource_access roles: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Reads realm_access.roles — fallback for realm-level role configurations.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRealmRoles(Map<String, Object> claims) {
        try {
            Object ra = claims.get(REALM_ACCESS);
            if (!(ra instanceof Map)) return Collections.emptyList();
            Object rolesObj = ((Map<?, ?>) ra).get(ROLES);
            if (rolesObj instanceof List) return (List<String>) rolesObj;
        } catch (Exception e) {
            log.debug("Could not extract realm_access roles: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
