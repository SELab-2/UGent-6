package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.model.Auth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Test de AuthTestController klasse direct door zijn methodes te laden
@SpringBootTest
public class StaticAuthControllerTest {
//    private final List<String> groepLijst = List.of("Groep 1");
//
//
//    private final User user = new User("Tester De Test", "Tester", "De Test",
//            "test.testing@gtest.test", groepLijst, "123456");
//    private final List<SimpleGrantedAuthority> authLijst = List.of(new SimpleGrantedAuthority("READ_AUTHORITY"));
//    private final Auth auth = new Auth(user, authLijst);
//
//    @Autowired
//    private AuthTestController authTestController;
//
//
//    @Test
//    void contextLoads() throws Exception {
//        assertThat(authTestController).isNotNull();
//    }
//
//    @Test
//    void pingPong() throws Exception {
//        assertEquals(authTestController.ping(), "Pong");
//    }
//
//    @Test
//    void indexTest() throws Exception {
//        assertEquals(authTestController.index(), "Running...");
//    }

//    Werkt voorlopig nog niet
//    @Test
//    void apiTest() throws Exception {
//        assertEquals(authTestController.testApi(auth), user);
//        assertEquals(authTestController.testApi(auth).groups, groepLijst);
//        assertEquals(authTestController.testApi(auth).name, "Tester De Test");
//    }
}