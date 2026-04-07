package com.cydeo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityFilterChain — URL-level role guards.
 *
 * Role mapping (set by KeycloakOidcUserService):
 *   Keycloak "Admin"    → ROLE_Admin
 *   Keycloak "Manager"  → ROLE_Manager
 *   Keycloak "Employee" → ROLE_Employee
 *
 * URL rules mirror backend @RolesAllowed at a coarse-grained level.
 * Fine-grained per-method control is applied via @PreAuthorize in controllers.
 *
 * Spring Boot 2.7.x / Spring Security 5.7.x:
 *   @EnableGlobalMethodSecurity(prePostEnabled = true) enables @PreAuthorize / @PostAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakOidcUserService keycloakOidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth

                // ── public ────────────────────────────────────────────────
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .antMatchers("/login**", "/oauth2/**", "/error**").permitAll()
                // Root is public; after login it redirects to /dashboard
                .antMatchers("/").permitAll()

                // ── protected: any authenticated user ─────────────────────
                .antMatchers("/dashboard").authenticated()

                // ── protected: Admin only ──────────────────────────────────
                // All UserController endpoints are @RolesAllowed("Admin")
                .antMatchers("/users/**").hasRole("Admin")

                // ── protected: Admin or Manager ───────────────────────────
                // ProjectController: Admin (list-all, count) + Manager (CRUD, start, complete)
                .antMatchers("/projects/**").hasAnyRole("Admin", "Manager")

                // ── protected: any app role ────────────────────────────────
                // TaskController: Manager (CRUD) + Employee (status update, own tasks)
                // Fine-grained control is enforced per-method via @PreAuthorize
                .antMatchers("/tasks/**").hasAnyRole("Admin", "Manager", "Employee")

                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/dashboard", true)
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(keycloakOidcUserService))
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
