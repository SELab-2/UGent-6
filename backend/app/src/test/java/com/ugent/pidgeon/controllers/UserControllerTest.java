package com.ugent.pidgeon.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends ControllerTest {

    @InjectMocks
    private UserController userController;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    //@Test
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
    }
}