package com.ugent.pidgeon.model;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


public class AuthTest {

    private final User testUser = new User("John Doe", "John", "Doe", "john.doe@gmail.com", "123456", "");

    private final List<SimpleGrantedAuthority> authLijst = List.of(new SimpleGrantedAuthority("READ_AUTHORITY"));
    private final Auth auth = new Auth(testUser, authLijst);

    @Test
    public void nameTest(){
        Assertions.assertEquals(auth.getName(), "John Doe");
    }

    @Test
    public void emailTest(){
        Assertions.assertEquals(auth.getEmail(), "john.doe@gmail.com");
    }

    @Test
    public void oidTest(){
        Assertions.assertEquals(auth.getCredentials(), "123456");
    }

    @Test
    public void userTest(){
        Assertions.assertEquals(auth.getUser(), testUser);
    }

}
