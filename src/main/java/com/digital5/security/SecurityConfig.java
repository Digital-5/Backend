package com.digital5.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    BearerTokenAuthFilter bearerTokenAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(request -> {
                    request.requestMatchers("/account/register").permitAll();
                    request.anyRequest().authenticated();
                })
                // place the bearer token filter before basic auth so it can populate the SecurityContext
                .addFilterBefore(bearerTokenAuthFilter, BasicAuthenticationFilter.class);
        return http.build();
    }

}
