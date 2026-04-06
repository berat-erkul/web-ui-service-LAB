package com.cydeo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources — no login required
                .antMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Spring OAuth2 / error plumbing
                .antMatchers("/login**", "/oauth2/**", "/error**").permitAll()
                // Protected pages
                .antMatchers("/dashboard", "/users/**", "/projects/**").authenticated()
                // Root redirect — permit so the browser can follow to /dashboard
                .antMatchers("/").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/dashboard", true)
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
