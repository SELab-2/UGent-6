package com.ugent.pidgeon.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

import com.ugent.pidgeon.GlobalErrorHandler;
import com.ugent.pidgeon.auth.RolesInterceptor;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.User;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.UserRepository;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ControllerTest {

    protected MockMvc mockMvc;

    private UserEntity mockUser;

    @Mock
    protected UserRepository userRepository;

    RolesInterceptor rolesInterceptor;

    @BeforeEach
    public void testSetUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User("displayName", "firstName", "lastName", "email", "test", "studentnummer");
        Auth authUser = new Auth(user, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authUser);

        // Only stubbing necessary methods for the test
        mockUser = new UserEntity(
            user.firstName,
            user.lastName,
            user.email,
            UserRole.teacher,
            user.oid,
            "studentnummer"
        );
        mockUser.setId(1L);
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
