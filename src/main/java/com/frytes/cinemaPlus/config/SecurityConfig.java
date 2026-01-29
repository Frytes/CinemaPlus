package com.frytes.cinemaPlus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/movies/**", "/api/halls/**", "/api/sessions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/session/*/seats").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/movies/**", "/api/halls/**", "/api/sessions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**", "/api/halls/**", "/api/sessions/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**", "/api/halls/**", "/api/sessions/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/pricing/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/simulation/*").hasAuthority("ADMIN")

                        .requestMatchers("/api/bookings/**").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}