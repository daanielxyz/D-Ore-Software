package com.dore.auth;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

/** Login de la demo: un solo usuario, sesión por cookie y protección CSRF. */
@Configuration
class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/login", "/api/auth/csrf").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.spa())
                .formLogin(login -> login
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, user) -> response.setStatus(204))
                        .failureHandler((request, response, error) -> response.setStatus(401)))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .httpBasic(basic -> basic.disable())
                .headers(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** Sin APP_PASSWORD se genera una clave temporal, para no dejar una fija en el código. */
    @Bean
    UserDetailsService users(@Value("${app.user}") String name,
            @Value("${app.password:}") String password, PasswordEncoder encoder) {
        String secret = password;
        if (secret.isBlank()) {
            secret = UUID.randomUUID().toString().substring(0, 12);
            log.warn("APP_PASSWORD sin definir. Clave temporal para el usuario '{}': {}", name, secret);
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(name).password(encoder.encode(secret)).roles("ADMIN").build());
    }
}
