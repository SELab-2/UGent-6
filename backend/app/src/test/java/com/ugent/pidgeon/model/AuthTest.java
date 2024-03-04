package com.ugent.pidgeon.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@SpringBootTest
public class AuthTest {

    private final List<String> groeplijst = List.of("testgroep1", "testgroep2");
    private final User testUser = new User("John Doe", "john.doe@gmail.com", groeplijst, "123456");

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
    public void groupTest(){
        Assertions.assertEquals(auth.getGroups().size(), 2);
        Assertions.assertEquals(auth.getGroups().get(0), "testgroep1");
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
