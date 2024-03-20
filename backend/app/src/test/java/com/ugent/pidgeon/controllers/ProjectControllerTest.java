package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.ProjectJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectControllerTest {

    protected MockMvc mockMvc;

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseUserRepository courseUserRepository;

    @Mock
    private GroupClusterRepository groupClusterRepository;

    @Mock
    private TestRepository testRepository;

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

    @Test
    public void testCreateProject() {
        // Mock data
        long courseId = 1L;
        ProjectJson projectJson = new ProjectJson("Test Project", "Test Description", 1, 1, true, 100, Timestamp.valueOf(LocalDateTime.MIN));

        Auth auth = mock(Auth.class);
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(auth.getUserEntity()).thenReturn(user);

        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);

        // Mock repository behavior
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
        when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
        when(testRepository.existsById(projectJson.getTestId())).thenReturn(true);

        // Call controller method
        ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson, auth);

        // Verify response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
