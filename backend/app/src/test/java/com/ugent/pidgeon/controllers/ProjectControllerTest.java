package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectControllerTest {

    protected MockMvc mockMvc;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .defaultRequest(MockMvcRequestBuilders.get("/**")
                        .with(request -> { request.setUserPrincipal(SecurityContextHolder.getContext().getAuthentication()); return request; }))
                .build();

    }

    @Test
    void getProjects_ReturnsProjects_WhenCalled() {
        // Arrange
        Auth auth = mock(Auth.class);
        ProjectEntity project = new ProjectEntity();
        project.setName("Project 1");
        project.setId(1L);
        List<ProjectEntity> projects = new ArrayList<>();
        projects.add(project);
        when(projectRepository.findProjectsByUserId(anyLong())).thenReturn(projects);
        UserEntity user = new UserEntity("Test", "De Tester", "test.tester@test.com", UserRole.student, "azure");
        user.setId(1L);
        when(auth.getUserEntity()).thenReturn(user);

        // Act
        ResponseEntity<?> response = projectController.getProjects(auth);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Additional Assertions
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);

        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(1, responseBody.size());

        // Simplifying the response a bit but the point stands
        assertEquals("{name=Project 1, url=/api/projects/1}", responseBody.get(0).toString());
    }
}