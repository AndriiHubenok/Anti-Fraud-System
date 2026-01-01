package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RestAuthenticationEntryPoint restAuthenticationEntryPoint) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .csrf(CsrfConfigurer::disable)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(("/api/antifraud/suspicious-ip/**")).hasRole("SUPPORT")
                        .requestMatchers(("/api/antifraud/stolencard")).hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction/**").hasRole("MERCHANT")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction/**").hasRole("SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role/**").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/auth/list/**").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }
}
