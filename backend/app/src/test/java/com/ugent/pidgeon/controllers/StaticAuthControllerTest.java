package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticAuthControllerTest {

    private AuthTestController authTestController;
    private final User user = new User("Tester De Test", "Tester", "De Test",
            "test.testing@gtest.test", "123456");
    private final List<SimpleGrantedAuthority> authLijst = List.of(new SimpleGrantedAuthority("READ_AUTHORITY"));
    private final Auth auth = new Auth(user, authLijst);

    @BeforeEach
    void setUp() {
        authTestController = new AuthTestController();
    }

    @Test
    void contextLoads() throws Exception {
        // Test the context loading
        assert(authTestController != null);
    }

    @Test
    void pingPong() throws Exception {
        // Test the ping method
        assertEquals(authTestController.ping().getBody(), "Pong");
    }

    @Test
    void indexTest() throws Exception {
        // Test the index method
        assertEquals(authTestController.index().getBody(), "Running...");
    }

    @Test
    void testApi() throws Exception {
        // Mock necessary objects
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // Call the method to be tested
        User result = authTestController.testApi(request, auth);

        // Assert the result
        assertEquals(auth.getUser(), result);
    }

    @Test
    void postTest() throws Exception {
        // Mock necessary objects
        Object requestBody = new Object(); // You may need to create a mock request body as per your requirements

        // Call the method to be tested
        Object result = authTestController.postTest(requestBody);

        // Assert the result
        assertEquals(requestBody, result);
    }
}