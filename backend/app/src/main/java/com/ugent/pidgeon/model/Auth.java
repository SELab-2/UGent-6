package com.ugent.pidgeon.model;

import com.ugent.pidgeon.postgre.models.UserEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;
import com.ugent.pidgeon.model.User;
public class Auth extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 620L;

    private final User user;
    private UserEntity userEntity;



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
    public String getStudentNumber() { return user.studentnumber; }

    public String getOid(){
        return user.oid;
    }

    public void setUserEntity(UserEntity user){
        userEntity = user;
    }

    public UserEntity getUserEntity(){
        return userEntity;
    }

    public static UsernamePasswordAuthenticationToken unauthenticated(Object principal, Object credentials) {
        return new UsernamePasswordAuthenticationToken(principal, credentials);
    }

    public static UsernamePasswordAuthenticationToken authenticated(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        return new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
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

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
