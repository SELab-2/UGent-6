package com.ugent.pidgeon.model;

import com.ugent.selab2.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserTest {

    private final List<String> groeplijst = List.of("testgroep1", "testgroep2");
    private final User testUser = new User("John Doe", "john.doe@gmail.com", groeplijst, "123456");

    @Test
    public void isNotNull(){
        Assertions.assertNotNull(testUser);
    }

}
