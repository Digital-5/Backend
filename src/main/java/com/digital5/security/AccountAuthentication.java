package com.digital5.security;

import com.digital5.entity.AccountEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AccountAuthentication extends AbstractAuthenticationToken {
    private final AccountEntity principal;

    public AccountAuthentication(AccountEntity principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return principal; }
}
