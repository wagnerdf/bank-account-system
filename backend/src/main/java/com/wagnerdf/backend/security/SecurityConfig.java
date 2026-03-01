package com.wagnerdf.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.wagnerdf.backend.filter.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    // Password encoder para codificar senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager exposto para login manual
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Configura rotas e autenticação
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    	http 
    		.csrf(csrf -> csrf.disable()) 
    		.authorizeHttpRequests(auth -> auth 
    				.requestMatchers("/auth/**", "/h2-console/**").permitAll() 
    				.anyRequest().authenticated()
    		) 
    		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); 
    	
    	http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
    	return http.build();
    }
}