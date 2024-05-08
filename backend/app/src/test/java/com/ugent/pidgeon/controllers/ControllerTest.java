package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.GlobalErrorHandler;
import com.ugent.pidgeon.auth.RolesInterceptor;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import java.util.logging.Logger;
import org.apache.juli.logging.Log;
import org.hibernate.annotations.Check;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ControllerTest {

    protected MockMvc mockMvc;

    private UserEntity mockUser;

    @Mock
    protected UserRepository userRepository;

    RolesInterceptor rolesInterceptor;

    @BeforeEach
    public void testSetUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User("displayName", "firstName", "lastName", "email", "test");
        Auth authUser = new Auth(user, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authUser);

        // Only stubbing necessary methods for the test
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setRole(UserRole.teacher);
        authUser.setUserEntity(mockUser);
        lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        lenient().when(userRepository.findCourseIdsByUserId(anyLong())).thenReturn(new ArrayList<>());
        lenient().when(userRepository.findUserByAzureId("test")).thenReturn(Optional.of(mockUser));
        Logger.getGlobal().info("User: " + mockUser);

        rolesInterceptor = new RolesInterceptor(userRepository);
        //  when(userRepository.findUserByAzureId(anyString())).thenReturn(userEntity);
    }

    protected void setUpController(Object controller) {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .addInterceptors(rolesInterceptor)
            .setControllerAdvice(new GlobalErrorHandler())
            .defaultRequest(MockMvcRequestBuilders.get("/**")
                .with(request -> {
                    request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication());
                    return request;
                }))
            .build();
    }

    protected UserEntity getMockUser() {
        return mockUser;
    }

    protected void setMockUserRoles(UserRole role) {
        mockUser.setRole(role);
    }

}
