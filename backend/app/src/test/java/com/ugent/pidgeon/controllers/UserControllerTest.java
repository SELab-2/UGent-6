package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * This class is used to test the UserController.
 * It uses the Spring Boot Test framework to provide an application context for the tests.
 * The UserRepository is mocked to isolate the UserController from the database.
 * The authFilter is disabled, meaning we only do checks for roles.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private UserEntity mockUser(){
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setRole(UserRole.student);
        return userEntity;
    }

    /**
     * This method is executed before each test.
     * It sets up the SecurityContextHolder with a User and Auth object.
     * It also mocks the UserRepository to return a UserEntity when findUserByAzureId is called (which gets called while checking the role).
     */
    @BeforeEach
    public void setUp() {
        User user = new User("displayName", "firstName", "lastName", "email", "test");
        Auth authUser = new Auth(user, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authUser);
        UserEntity userEntity = mockUser();
        when(userRepository.findUserByAzureId(anyString())).thenReturn(userEntity);
    }

    /**
     * This test method tests the getUserById method of the UserController.
     * It performs a GET request to the getUserById endpoint and asserts that the response status is OK.
     * @throws Exception - if any error occurs during the request
     */
    @Test
    public void testGetUserById() throws Exception {

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser())); // mocks the UserRepository to return a UserEntity when findById is called
        when(userRepository.findCourseIdsByUserId(anyLong())).thenReturn(new ArrayList<>()); // mocks the UserRepository to return an (empty) list of course ids when findCourseIdsByUserId is called


        long userId = 1L;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USER_BASE_PATH + "/" + userId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.emptyString()))) // If body is empty, it means it returned null
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response body: " + responseBody);
        // Maybe add more tests here for the response body
    }

}
