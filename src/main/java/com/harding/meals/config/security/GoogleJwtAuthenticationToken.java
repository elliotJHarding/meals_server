package com.harding.meals.config.security;

import com.harding.meals.entity.AppUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.List;

public class GoogleJwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final AppUser principal;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public GoogleJwtAuthenticationToken(AppUser principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
    }

    public static GoogleJwtAuthenticationToken unauthenticated(String token) {
        return new GoogleJwtAuthenticationToken(null, token, null);
    }

    public static GoogleJwtAuthenticationToken authenticated(AppUser principal, String token, List<GrantedAuthority> authorities) {
        GoogleJwtAuthenticationToken idToken = new GoogleJwtAuthenticationToken(principal, token, authorities);
        idToken.setAuthenticated(true);
        return idToken;
    }

    @Override
    public String getCredentials() {
        return this.token;
    }

    @Override
    public AppUser getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean implies(Subject subject) {
        return super.implies(subject);
    }
}
