package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

public class ControllerTest {

    protected MockMvc mockMvc;


    @Mock
    protected UserRepository userRepository;

    @BeforeEach
    public void testSetUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User("displayName", "firstName", "lastName", "email", "test");
        Auth authUser = new Auth(user, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authUser);


        // Only stubbing necessary methods for the test
        UserEntity userEntity = mockUser();
        authUser.setUserEntity(userEntity);
        lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.of(userEntity));
        lenient().when(userRepository.findCourseIdsByUserId(anyLong())).thenReturn(new ArrayList<>());
      //  when(userRepository.findUserByAzureId(anyString())).thenReturn(userEntity);


    }

    protected UserEntity mockUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setRole(UserRole.student);
        return userEntity;
    }


}
