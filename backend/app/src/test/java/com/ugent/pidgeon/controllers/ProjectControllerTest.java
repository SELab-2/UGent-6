package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.ProjectResponseJson;
import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.ProjectJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.ClusterUtil;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.CourseUtil;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.ProjectUtil;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
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
  private EntityToJsonConverter entityToJsonConverter;

  @Mock
  private CommonDatabaseActions commonDatabaseActions;

  @Mock
  private ClusterUtil clusterUtil;

  @Mock
  private GroupRepository grouprRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  void testGetProjectShouldReturnOneProject() {
    // Mock data
    Auth auth = mock(Auth.class);
    ProjectEntity project = new ProjectEntity();
    project.setName("Project 1");
    project.setId(1L);
    List<ProjectEntity> projects = new ArrayList<>();
    projects.add(project);
    UserEntity user = new UserEntity("Test", "De Tester", "test.tester@test.com", UserRole.student,
        "azure");
    user.setId(1L);

    // Mock repository behavior
    when(projectRepository.findProjectsByUserId(anyLong())).thenReturn(projects);
    when(auth.getUserEntity()).thenReturn(user);

    // Call controller method
    ResponseEntity<?> response = projectController.getProjects(auth);

    // Verify response
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(List.class, response.getBody());
    List<?> responseBody = (List<?>) response.getBody();
    assertEquals(1, responseBody.size());
    assertEquals("{name=Project 1, url=/api/projects/1}", responseBody.get(0).toString());

  }

  @Test
  void testGetProjectShouldReturnMultipleProject() {
    // Mock data
    Auth auth = mock(Auth.class);
    ProjectEntity project1 = new ProjectEntity();
    project1.setName("Project 1");
    project1.setId(1L);
    ProjectEntity project2 = new ProjectEntity();
    project2.setName("Project 2");
    project2.setId(2L);
    List<ProjectEntity> projects = new ArrayList<>();
    projects.add(project1);
    projects.add(project2);
    UserEntity user = new UserEntity("Test", "De Tester", "test.tester@test.com", UserRole.student,
        "azure");
    user.setId(1L);

    // Mock repository behavior
    when(projectRepository.findProjectsByUserId(anyLong())).thenReturn(projects);
    when(auth.getUserEntity()).thenReturn(user);

    // Call controller method
    ResponseEntity<?> response = projectController.getProjects(auth);

    // Verify response
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(List.class, response.getBody());
    List<?> responseBody = (List<?>) response.getBody();
    assertEquals(2, responseBody.size());
    assertEquals("{name=Project 1, url=/api/projects/1}", responseBody.get(0).toString());
    assertEquals("{name=Project 2, url=/api/projects/2}", responseBody.get(1).toString());
  }


  @Test
  void testGetProjectByIdShouldReturnProject() {
    // Mock data
    // auth object
    Auth auth = mock(Auth.class);
    // projects
    ProjectEntity project1 = new ProjectEntity();
    project1.setName("Project 1");
    project1.setId(1L);
    ProjectEntity project2 = new ProjectEntity();
    project2.setName("Project 2");
    project2.setId(2L);
    project2.setCourseId(1L);
    ProjectEntity project3 = new ProjectEntity();
    project3.setName("Project 3");
    project3.setId(3L);
    List<ProjectEntity> projects = new ArrayList<>();
    projects.add(project1);
    projects.add(project2);
    projects.add(project3);
    // users
    UserEntity user = new UserEntity("Test", "De Tester", "test.tester@test.com", UserRole.student,
        "azure");
    user.setId(1L);
    //check results
    CourseEntity courseEntity = new CourseEntity();
    CheckResult<ProjectEntity> checkResult = new CheckResult<>(HttpStatus.OK, "TestProject",
        project2);
    CheckResult<CourseEntity> courseCheck = new CheckResult<>(HttpStatus.OK, "TestCourse",
        courseEntity);

    // Mock repository behavior
    when(projectUtil.canGetProject(2L, user)).thenReturn(checkResult);
    when(courseUtil.getCourseIfExists(1L)).thenReturn(courseCheck);
    when(auth.getUserEntity()).thenReturn(user);
    when(entityToJsonConverter.projectEntityToProjectResponseJson(project2, courseCheck.getData(),
        user)).thenReturn(new ProjectResponseJson(
        new CourseReferenceJson("TestCourse", ApiRoutes.COURSE_BASE_PATH + "/" + 1L, 1L),
        OffsetDateTime.MAX,
        "Test", 2L, "TestProject", "testUrl", "testUrl", 0, true, new ProjectProgressJson(0, 0)));

    // Call controller method
    ResponseEntity<?> response = projectController.getProjectById(2L, auth);
    // Verify response
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ProjectResponseJson responseBody = (ProjectResponseJson) response.getBody();
    assert responseBody != null;
    assertEquals(2L, responseBody.projectId());

  }


  @Test
  void testGetProjectByIdShouldFailReasonCanNotGetProject() {
    // Mock data
    // auth object
    Auth auth = mock(Auth.class);
    // projects
    ProjectEntity project1 = new ProjectEntity();
    project1.setName("Project 1");
    project1.setId(1L);
    ProjectEntity project2 = new ProjectEntity();
    project2.setName("Project 2");
    project2.setId(2L);
    project2.setCourseId(1L);
    ProjectEntity project3 = new ProjectEntity();
    project3.setName("Project 3");
    project3.setId(3L);
    List<ProjectEntity> projects = new ArrayList<>();
    projects.add(project1);
    projects.add(project2);
    projects.add(project3);
    // users
    UserEntity user = new UserEntity("Test", "De Tester", "test.tester@test.com", UserRole.student,
        "azure");
    user.setId(1L);
    //check results
    CourseEntity courseEntity = new CourseEntity();
    CheckResult<ProjectEntity> checkResult = new CheckResult<>(HttpStatus.FORBIDDEN,
        "testProjectForbidden",
        project2);
    CheckResult<CourseEntity> courseCheck = new CheckResult<>(HttpStatus.OK, "TestCourse",
        courseEntity);

    // Mock repository behavior
    when(projectUtil.canGetProject(2L, user)).thenReturn(checkResult);
    when(courseUtil.getCourseIfExists(1L)).thenReturn(courseCheck);
    when(auth.getUserEntity()).thenReturn(user);
    when(entityToJsonConverter.projectEntityToProjectResponseJson(project2, courseCheck.getData(),
        user)).thenReturn(new ProjectResponseJson(
        new CourseReferenceJson("TestCourse", ApiRoutes.COURSE_BASE_PATH + "/" + 1L, 1L),
        OffsetDateTime.MAX,
        "Test", 2L, "TestProject", "testUrl", "testUrl", 0, true, new ProjectProgressJson(0, 0)));

    // Call controller method
    ResponseEntity<?> response = projectController.getProjectById(2L, auth);
    // Verify response
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("testProjectForbidden", Objects.requireNonNull(response.getBody()).toString());

  }


  @Test
  void testGetProjectByIdShouldFailReasonCanNotGetCourse() {
    // Mock data
    // auth object
    Auth auth = mock(Auth.class);
    // projects
    ProjectEntity project1 = new ProjectEntity();
    project1.setName("Project 1");
    project1.setId(1L);
    ProjectEntity project2 = new ProjectEntity();
    project2.setName("Project 2");
    project2.setId(2L);
    project2.setCourseId(1L);
    ProjectEntity project3 = new ProjectEntity();
    project3.setName("Project 3");
    project3.setId(3L);
    List<ProjectEntity> projects = new ArrayList<>();
    projects.add(project1);
    projects.add(project2);
    projects.add(project3);
    // users
    UserEntity user = new UserEntity("Test", "De Tester", "test.tester@test.com", UserRole.student,
        "azure");
    user.setId(1L);
    //check results
    CourseEntity courseEntity = new CourseEntity();
    CheckResult<ProjectEntity> checkResult = new CheckResult<>(HttpStatus.OK, "TestProject",
        project2);
    CheckResult<CourseEntity> courseCheck = new CheckResult<>(HttpStatus.FORBIDDEN,
        "testCourseForbidden",
        courseEntity);

    // Mock repository behavior
    when(projectUtil.canGetProject(2L, user)).thenReturn(checkResult);
    when(courseUtil.getCourseIfExists(1L)).thenReturn(courseCheck);
    when(auth.getUserEntity()).thenReturn(user);
    when(entityToJsonConverter.projectEntityToProjectResponseJson(project2, courseCheck.getData(),
        user)).thenReturn(new ProjectResponseJson(
        new CourseReferenceJson("TestCourse", ApiRoutes.COURSE_BASE_PATH + "/" + 1L, 1L),
        OffsetDateTime.MAX,
        "Test", 2L, "TestProject", "testUrl", "testUrl", 0, true, new ProjectProgressJson(0, 0)));

    // Call controller method
    ResponseEntity<?> response = projectController.getProjectById(2L, auth);
    // Verify response
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("testCourseForbidden", Objects.requireNonNull(response.getBody()).toString());

  }

  @Test
  public void testCreateProjectShouldMakeProject() {
    // Mock data
    long courseId = 1L;
    ProjectJson projectJson =
        new ProjectJson("Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX);
    ProjectEntity projectEntity =
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX);
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(1L);
    when(auth.getUserEntity()).thenReturn(user);

    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);

    CheckResult<CourseEntity> checkAcces = new CheckResult<>(HttpStatus.OK, "TestIsAdmin",
        courseEntity);

    CheckResult<Void> checkResult = new CheckResult<>(HttpStatus.OK, "TestProjectJson", null);

    // Mock repository behavior
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

    when(courseUtil.getCourseIfAdmin(courseId, user)).thenReturn(checkAcces);
    when(projectUtil.checkProjectJson(projectJson, courseId)).thenReturn(checkResult);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
    when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(
        Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
    when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(
        Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
    when(projectRepository.save(ArgumentMatchers.any(ProjectEntity.class))).thenReturn(
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX));
    // Call controller method
    ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson,
        auth);

    // Verify response
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }


  @Test
  public void testCreateProjectShouldFailReasonCanNotGetCourse() {
    // Mock data
    long courseId = 1L;
    ProjectJson projectJson =
        new ProjectJson("Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX);
    ProjectEntity projectEntity =
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX);
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(1L);
    when(auth.getUserEntity()).thenReturn(user);

    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);

    CheckResult<CourseEntity> checkAcces = new CheckResult<>(HttpStatus.FORBIDDEN, "TestIsAdmin",
        courseEntity);

    CheckResult<Void> checkResult = new CheckResult<>(HttpStatus.OK, "TestProjectJson", null);

    // Mock repository behavior
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

    when(courseUtil.getCourseIfAdmin(courseId, user)).thenReturn(checkAcces);
    when(projectUtil.checkProjectJson(projectJson, courseId)).thenReturn(checkResult);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
    when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(
        Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
    when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(
        Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
    when(projectRepository.save(ArgumentMatchers.any(ProjectEntity.class))).thenReturn(
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX));
    // Call controller method
    ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson,
        auth);

    // Verify response
    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }

  @Test
  public void testCreateProjectShouldFailReasonCanNotGetProjectJson() {
    // Mock data
    long courseId = 1L;
    ProjectJson projectJson =
        new ProjectJson("Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX);
    ProjectEntity projectEntity =
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX);
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(1L);
    when(auth.getUserEntity()).thenReturn(user);

    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);

    CheckResult<CourseEntity> checkAcces = new CheckResult<>(HttpStatus.OK, "TestIsAdmin",
        courseEntity);

    CheckResult<Void> checkResult = new CheckResult<>(HttpStatus.FORBIDDEN, "TestProjectJson",
        null);

    // Mock repository behavior
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

    when(courseUtil.getCourseIfAdmin(courseId, user)).thenReturn(checkAcces);
    when(projectUtil.checkProjectJson(projectJson, courseId)).thenReturn(checkResult);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
    when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(
        Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
    when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(
        Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
    when(projectRepository.save(ArgumentMatchers.any(ProjectEntity.class))).thenReturn(
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX));
    // Call controller method
    ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson,
        auth);

    // Verify response
    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }

  @Test
  public void testCreateProjectShouldFailReasonInternalServer1() {
    // Mock data
    long courseId = 1L;
    ProjectJson projectJson =
        new ProjectJson("Test Project", "Test Description", 1L, 1L, true, 100, OffsetDateTime.MAX);
    ProjectEntity projectEntity =
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX);
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(1L);
    when(auth.getUserEntity()).thenReturn(user);

    CheckResult<Void> checkResult = new CheckResult<>(HttpStatus.FORBIDDEN, "TestProjectJson",
        null);

    // Mock repository behavior
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

    when(projectUtil.checkProjectJson(projectJson, courseId)).thenReturn(checkResult);
    when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(
        Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
    when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(
        Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
    when(projectRepository.save(ArgumentMatchers.any(ProjectEntity.class))).thenReturn(
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX));
    // Call controller method
    ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson,
        auth);

    // Verify response
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
  }


  @Test
  public void testCreateProjectShouldFailReasonInternalServer2() {
    // Mock data
    long courseId = 1L;
    ProjectJson projectJson =
        new ProjectJson("Test Project", "Test Description", null, 1L, true, 100,
            OffsetDateTime.MAX);
    ProjectEntity projectEntity =
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX);
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(1L);
    when(auth.getUserEntity()).thenReturn(user);

    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);

    CheckResult<CourseEntity> checkAcces = new CheckResult<>(HttpStatus.OK, "TestIsAdmin",
        courseEntity);

    CheckResult<Void> checkResult = new CheckResult<>(HttpStatus.OK, "TestProjectJson",
        null);

    // Mock repository behavior
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
    when(courseUtil.getCourseIfAdmin(courseId, user)).thenReturn(checkAcces);
    when(projectUtil.checkProjectJson(projectJson, courseId)).thenReturn(checkResult);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
    when(courseUserRepository.findById(ArgumentMatchers.any(CourseUserId.class))).thenReturn(
        Optional.of(new CourseUserEntity(1, 1, CourseRelation.course_admin)));
    when(groupClusterRepository.findById(projectJson.getGroupClusterId())).thenReturn(
        Optional.of(new GroupClusterEntity(1L, 20, "Testcluster", 10)));
    when(projectRepository.save(ArgumentMatchers.any(ProjectEntity.class))).thenReturn(
        new ProjectEntity(1, "Test Project", "Test Description", 1L, 1L, true, 100,
            OffsetDateTime.MAX));

    // Call controller method
    ResponseEntity<Object> responseEntity = projectController.createProject(courseId, projectJson,
        auth);

    // Verify response
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertEquals("Internal error while creating project without group, contact an administrator",
        responseEntity.getBody());
  }


  @Test
  void testPutProjectByIdShouldUpdateProject() {
    // Mock data
    long projectId = 1L;
    long userId = 1L;
    long courseId = 1L;
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(userId);
    ProjectEntity projectEntity = new ProjectEntity(1, "Test Project", "old description", 1L, 1L,
        false, 100, OffsetDateTime.MAX);
    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);
    ProjectJson projectJson = new ProjectJson("Test Project", "new description", 1L, 1L, true, 100,
        OffsetDateTime.MAX);

    ProjectEntity newProjectEntity = new ProjectEntity(1, "Test Project", "new description", 1L, 1L,
        true, 100, OffsetDateTime.MAX);

    CheckResult<ProjectEntity> checkResult = new CheckResult<>(HttpStatus.OK, "TestProject", projectEntity);
    CheckResult<Void> checkProject = new CheckResult<>(HttpStatus.OK, "TestProjectJson", null);

    // Mock behavior
    when(auth.getUserEntity()).thenReturn(user);
    when(projectUtil.getProjectIfAdmin(projectId, user)).thenReturn(checkResult);
    when(projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId())).thenReturn(checkProject);
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
    when(courseRepository.findById(projectId)).thenReturn(Optional.of(courseEntity));
    when(entityToJsonConverter.projectEntityToProjectResponseJson(any(), any(), any())).thenReturn(new ProjectResponseJson(
        new CourseReferenceJson("TestCourse", ApiRoutes.COURSE_BASE_PATH + "/" + 1L, 1L),
        OffsetDateTime.MAX,
        "Test", 2L, "TestProject", "testUrl", "testUrl", 0, true, new ProjectProgressJson(0, 0)));
    // Call controller method
    ResponseEntity<?> responseEntity = projectController.putProjectById(projectId, projectJson, auth);

    // Verify response
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());


  }

  @Test
  void testPatchProjectByIdShouldUpdateProject(){
    // Mock data
    long projectId = 1L;
    long userId = 1L;
    long courseId = 1L;
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(userId);
    ProjectEntity projectEntity = new ProjectEntity(1, "Test Project", "old description", 1L, 1L,
        false, 100, OffsetDateTime.MAX);
    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);
    ProjectJson projectJson = new ProjectJson("Test Project", "new description", null, 1L, true, 100,
        OffsetDateTime.MAX);

    ProjectEntity newProjectEntity = new ProjectEntity(1, "Test Project", "new description", 1L, 1L,
        true, 100, OffsetDateTime.MAX);

    CheckResult<ProjectEntity> checkResult = new CheckResult<>(HttpStatus.OK, "TestProject", projectEntity);
    CheckResult<Void> checkProject = new CheckResult<>(HttpStatus.OK, "TestProjectJson", null);

    // Mock behavior
    when(auth.getUserEntity()).thenReturn(user);
    when(projectUtil.getProjectIfAdmin(projectId, user)).thenReturn(checkResult);
    when(projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId())).thenReturn(checkProject);
    when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
    when(courseRepository.findById(projectId)).thenReturn(Optional.of(courseEntity));
    when(entityToJsonConverter.projectEntityToProjectResponseJson(any(), any(), any())).thenReturn(new ProjectResponseJson(
        new CourseReferenceJson("TestCourse", ApiRoutes.COURSE_BASE_PATH + "/" + 1L, 1L),
        OffsetDateTime.MAX,
        "Test", 2L, "TestProject", "testUrl", "testUrl", 0, true, new ProjectProgressJson(0, 0)));
    // Call controller method
    ResponseEntity<?> responseEntity = projectController.patchProjectById(projectId, projectJson, auth);

    // Verify response
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }


  @Test
  void testDeleteProjectByIdShouldDeleteProject() {
    // Mock data
    long projectId = 1L;
    long userId = 1L;
    long courseId = 1L;
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(userId);
    ProjectEntity projectEntity = new ProjectEntity(1, "Test Project", "old description", 1L, 1L,
        false, 100, OffsetDateTime.MAX);
    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);

    CheckResult<ProjectEntity> projectCheck = new CheckResult<>(HttpStatus.OK, "TestProject", projectEntity);
    CheckResult<Void> deleteResult = new CheckResult<>(HttpStatus.OK, "TestDelete", null);
    // Mock behavior
    when(auth.getUserEntity()).thenReturn(user);
    when(projectUtil.getProjectIfAdmin(projectId, user)).thenReturn(projectCheck);
    when(commonDatabaseActions.deleteProject(projectId)).thenReturn(deleteResult);

    // Call controller method
    ResponseEntity<?> responseEntity = projectController.deleteProjectById(projectId, auth);

    // Verify response
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  void testDeleteProjectByIdShouldFailReasonCanNotGetProject() {
    // Mock data
    long projectId = 1L;
    long userId = 1L;
    long courseId = 1L;
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(userId);
    ProjectEntity projectEntity = new ProjectEntity(1, "Test Project", "old description", 1L, 1L,
        false, 100, OffsetDateTime.MAX);
    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);

    CheckResult<ProjectEntity> projectCheck = new CheckResult<>(HttpStatus.FORBIDDEN, "TestProject", projectEntity);
    CheckResult<Void> deleteResult = new CheckResult<>(HttpStatus.OK, "TestDelete", null);
    // Mock behavior
    when(auth.getUserEntity()).thenReturn(user);
    when(projectUtil.getProjectIfAdmin(projectId, user)).thenReturn(projectCheck);
    when(commonDatabaseActions.deleteProject(projectId)).thenReturn(deleteResult);

    // Call controller method
    ResponseEntity<?> responseEntity = projectController.deleteProjectById(projectId, auth);

    // Verify response
    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }

  @Test
  void testGetGroupsOfProjectShouldReturnGroups() {
    // Mock data
    long projectId = 1L;
    long userId = 1L;
    long courseId = 1L;
    long groupId = 1L;
    Auth auth = mock(Auth.class);
    UserEntity user = new UserEntity();
    user.setId(userId);
    ProjectEntity projectEntity = new ProjectEntity(1, "Test Project", "old description", 1L, 1L,
        false, 100, OffsetDateTime.MAX);
    CourseEntity courseEntity = new CourseEntity();
    courseEntity.setId(courseId);
    List<Long> groupIds = new ArrayList<>();
    groupIds.add(groupId);
    List<GroupEntity> groups = new ArrayList<>();
    GroupEntity groupEntity = new GroupEntity();
    groupEntity.setId(groupId);
    groups.add(groupEntity);

    CheckResult<ProjectEntity> projectCheck = new CheckResult<>(HttpStatus.OK, "TestProject", projectEntity);
    CheckResult<List<GroupEntity>> groupCheck = new CheckResult<>(HttpStatus.OK, "TestGroups", groups);
    // Mock behavior
    when(auth.getUserEntity()).thenReturn(user);
    when(projectUtil.getProjectIfAdmin(projectId, user)).thenReturn(projectCheck);
    when(clusterUtil.isIndividualCluster(projectEntity.getGroupClusterId())).thenReturn(false);
    when(projectRepository.findGroupIdsByProjectId(projectId)).thenReturn(groupIds);
    when(grouprRepository.findById(groupId)).thenReturn(Optional.of(groupEntity));
    // Call controller method
    ResponseEntity<?> responseEntity = projectController.getGroupsOfProject(projectId, auth);

    // Verify response
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }




}
