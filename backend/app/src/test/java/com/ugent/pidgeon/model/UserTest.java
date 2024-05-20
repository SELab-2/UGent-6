package com.ugent.pidgeon.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTest {

    private final User testUser = new User("John Doe", "John", "Doe", "john.doe@gmail.com", "123456", "");

    @Test
    public void isNotNull() {
        Assertions.assertNotNull(testUser);
    }

    @Test
    public void testGetFullName() {
        String expectedFullName = "John Doe";
        Assertions.assertEquals(expectedFullName, testUser.name);
    }

    @Test
    public void testGetFirstName() {
        String expectedFirstName = "John";
        Assertions.assertEquals(expectedFirstName, testUser.firstName);
    }

    @Test
    public void testGetLastName() {
        String expectedLastName = "Doe";
        Assertions.assertEquals(expectedLastName, testUser.lastName);
    }

    @Test
    public void testGetEmail() {
        String expectedEmail = "john.doe@gmail.com";
        Assertions.assertEquals(expectedEmail, testUser.email);
    }

    @Test
    public void testGetOid() {
        String expectedOid = "123456";
        Assertions.assertEquals(expectedOid, testUser.oid);
    }
}

