package com.ugent.pidgeon.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ugent.pidgeon.CustomObjectMapper;
import com.ugent.pidgeon.model.json.UserJson;
import com.ugent.pidgeon.postgre.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest extends ControllerTest {

    @InjectMocks
    private UserController userController;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();
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


    @Test
    public void getUserByIdReturnsUserWhenUserExistsAndHasAccess() throws Exception {
        UserEntity userEntity = mockUser();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));

        MvcResult result = mockMvc.perform(get(ApiRoutes.USER_BASE_PATH + "/1"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        System.out.println("Response body: " + responseBody);

        ObjectMapper objectMapper = CustomObjectMapper.createObjectMapper();
        UserJson userJson = objectMapper.readValue(responseBody, UserJson.class);

        
        assertEquals(userEntity.getId(), userJson.getId());
        assertEquals(userEntity.getName(), userJson.getName());
        assertEquals(userEntity.getSurname(), userJson.getSurname());
        assertEquals(userEntity.getEmail(), userJson.getEmail());
        assertEquals(userEntity.getRole(), userJson.getRole());
    }

    @Test
    public void getUserByIdReturnsForbiddenWhenUserExistsButNoAccess() throws Exception {

        mockMvc.perform(get(ApiRoutes.USER_BASE_PATH + "/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUserByIdReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get(ApiRoutes.USER_BASE_PATH + "/1"))
                .andExpect(status().isNotFound());
    }



    @Test
    public void getUserByAzureIdReturnsUserWhenUserExists() throws Exception {
        //when(userRepository.findUserByAzureId(anyString())).thenReturn(Optional.of(mockUser()));

        mockMvc.perform(get(ApiRoutes.USER_AUTH_PATH))
                .andExpect(status().isOk());
    }
}