package com.ugent.selab2.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

public class Auth extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 620L;

    private final User user;



    public Auth(User user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;

        super.setAuthenticated(true);
    }

    public String getName(){
        return user.name;
    }
    public String getEmail(){
        return user.email;
    }

    public String getOid(){
        return user.oid;
    }

    public static org.springframework.security.authentication.UsernamePasswordAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, credentials);
    }

    public static org.springframework.security.authentication.UsernamePasswordAuthenticationToken authenticated(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, credentials, authorities);
    }

    public Object getCredentials() {
        return user.oid;
    }

    public Object getPrincipal() {
        return user;
    }

    public User getUser() {
        return user;
    }

    public List<String> getGroups() {return user.groups;}

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
