package com.ugent.pidgeon.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class ProjectControllerTest {

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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