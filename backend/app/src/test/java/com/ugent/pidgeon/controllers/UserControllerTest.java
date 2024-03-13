package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.controllers.ApiRoutes;
import com.ugent.pidgeon.controllers.UserController;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.ArrayList;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // Only stubbing necessary methods for the test
        UserEntity userEntity = mockUser();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));
        when(userRepository.findCourseIdsByUserId(anyLong())).thenReturn(new ArrayList<>());
    }

    private UserEntity mockUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setRole(UserRole.student);
        return userEntity;
    }

    /*@Test
    public void testGetUserById() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser()));
        when(userRepository.findCourseIdsByUserId(anyLong())).thenReturn(new ArrayList<>());

        long userId = 1L;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ApiRoutes.USER_BASE_PATH + "/" + userId))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.emptyString())))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Response body: " + responseBody);
        // Add more tests here for the response body if needed
    }*/
}