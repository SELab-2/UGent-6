package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.ProjectJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.ProjectUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;


import java.time.OffsetDateTime;
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
    private CourseUtil courseUtil;

    @Mock
    private ProjectUtil projectUtil;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    EntityToJsonConverter entityToJsonConverter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void test_getProjects() {
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
        System.out.println(response.getBody());
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
    public void test_createProject() {
        // Mock data
        long courseId = 1L;
        ProjectJson projectJson = new ProjectJson("Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX);
        ProjectEntity projectEntity = new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX);
        Auth auth = mock(Auth.class);
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(auth.getUserEntity()).thenReturn(user);

        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);

        CheckResult<CourseEntity> checkAcces = new CheckResult<>(HttpStatus.OK, "TestIsAdmin", courseEntity);

        CheckResult<Void> checkResult = new CheckResult<>(HttpStatus.OK, "TestProjectJson", null);

        // Mock repository behavior
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        when(courseUtil.getCourseIfAdmin(courseId, user)).thenReturn(checkAcces);
        when(projectUtil.checkProjectJson(projectJson, courseId)).thenReturn(checkResult);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
        when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
        when(projectRepository.save(ArgumentMatchers.any(ProjectEntity.class))).thenReturn(new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX));
        // Call controller method
        ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson, auth);


        // Verify response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
