package com.digital5.security;

import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import com.digital5.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Collections;

@Component
@AllArgsConstructor
public class BearerTokenAuthFilter extends OncePerRequestFilter {

    private JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ") && !authHeader.substring(7).isBlank()) {
            String accessToken = authHeader.substring(7);
            try {
                AccountEntity account = jwtService.verifyJWT(accessToken);
                if(account == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
                else {
                    Authentication authentication = new AccountAuthentication(account, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (DigitalException e) {
                response.setStatus(e.getStatusCode().value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}