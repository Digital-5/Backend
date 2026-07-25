package com.digital5.security;

import com.digital5.data.ErrorResponse;
import com.digital5.entity.AccountEntity;
import com.digital5.exception.DigitalException;
import com.digital5.logger.Logger;
import com.digital5.service.AccountService;
import com.digital5.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Extracts and verifies XEdDSA-signed JWTs from the Authorization header.
 * Sets the authenticated AccountEntity in the SecurityContext on success.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTService jwtService;
    private final AccountService accountService;

    public JwtAuthenticationFilter(JWTService jwtService, AccountService accountService) {
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            String uuid;
            try {
                uuid = jwtService.verifyJWT(token);
            } catch (DigitalException e) {
                Logger.logBackendException(e);
                ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode(), e.getMessage());
                response.setStatus(e.getStatusCode().value());
                response.setContentType("application/json");
                response.getWriter().write(errorResponse.toJsonString());
                return;
            }

            if (uuid == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication failed\"}");
                return;
            }

            AccountEntity account = accountService.getUserFromUUID(uuid);
            if (account == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Account not found\"}");
                return;
            }

            AccountAuthentication authentication = new AccountAuthentication(account, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

