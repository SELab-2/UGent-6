package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.model.json.ProjectJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectUtilTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ClusterUtil clusterUtil;

  @InjectMocks
  private ProjectUtil projectUtil;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testUserPartOfProject() {
    long projectId = 1L;
    long userId = 1L;
    when(projectRepository.userPartOfProject(projectId, userId)).thenReturn(true);
    boolean result = projectUtil.userPartOfProject(projectId, userId);
    assertEquals(true, result);
  }

  @Test
  public void testGetProjectIfExists() {
    long projectId = 1L;
    ProjectEntity projectEntity = new ProjectEntity();
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.of(projectEntity));
    CheckResult<ProjectEntity> result = projectUtil.getProjectIfExists(projectId);
    assertEquals(HttpStatus.OK, result.getStatus());
  }


  @Test
  public void testGetProjectIfExistsNotFound() {
    long projectId = 1L;
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.empty());
    CheckResult<ProjectEntity> result = projectUtil.getProjectIfExists(projectId);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }


  @Test
  public void testIsProjectAdmin() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.admin);
    when(projectRepository.adminOfProject(projectId, user.getId())).thenReturn(true);
    CheckResult<Void> result = projectUtil.isProjectAdmin(projectId, user);
    assertEquals(HttpStatus.OK, result.getStatus());
  }


  @Test
  public void testIsProjectAdminForbidden() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.student);
    when(projectRepository.adminOfProject(projectId, user.getId())).thenReturn(false);
    CheckResult<Void> result = projectUtil.isProjectAdmin(projectId, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
  }

  @Test
  public void testGetProjectIfAdmin() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.admin);
    ProjectEntity projectEntity = new ProjectEntity();
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.of(projectEntity));
    when(projectRepository.adminOfProject(projectId, user.getId())).thenReturn(true);
    CheckResult<ProjectEntity> result = projectUtil.getProjectIfAdmin(projectId, user);
    assertEquals(HttpStatus.OK, result.getStatus());
  }

  @Test
  public void testGetProjectIfAdminForbidden() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.student);
    ProjectEntity projectEntity = new ProjectEntity();
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.of(projectEntity));
    when(projectRepository.adminOfProject(projectId, user.getId())).thenReturn(false);
    CheckResult<ProjectEntity> result = projectUtil.getProjectIfAdmin(projectId, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
  }

  @Test
  public void testCheckProjectJson() {
    long courseId = 1L;
    ProjectJson projectJson = new ProjectJson();
    projectJson.setName("Test Project");
    projectJson.setDescription("This is a test project.");
    projectJson.setMaxScore(100);
    projectJson.setGroupClusterId(1L);
    projectJson.setDeadline(OffsetDateTime.now().plusDays(1));
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId)).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = projectUtil.checkProjectJson(projectJson, courseId);
    assertEquals(HttpStatus.OK, result.getStatus());
  }

  @Test
  public void testCheckProjectJsonNullName() {
    long courseId = 1L;
    ProjectJson projectJson = new ProjectJson();
    projectJson.setDescription("This is a test project.");
    projectJson.setMaxScore(100);
    projectJson.setGroupClusterId(1L);
    projectJson.setDeadline(OffsetDateTime.now().plusDays(1));
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId)).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = projectUtil.checkProjectJson(projectJson, courseId);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void testCheckProjectJsonNullDescription() {
    long courseId = 1L;
    ProjectJson projectJson = new ProjectJson();
    projectJson.setName("Test Project");
    projectJson.setMaxScore(100);
    projectJson.setGroupClusterId(1L);
    projectJson.setDeadline(OffsetDateTime.now().plusDays(1));
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId)).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = projectUtil.checkProjectJson(projectJson, courseId);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void testCheckProjectJsonNullMaxScore() {
    long courseId = 1L;
    ProjectJson projectJson = new ProjectJson();
    projectJson.setName("Test Project");
    projectJson.setDescription("This is a test project.");
    projectJson.setGroupClusterId(1L);
    projectJson.setDeadline(OffsetDateTime.now().plusDays(1));
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId)).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = projectUtil.checkProjectJson(projectJson, courseId);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }


  @Test
  public void testCheckProjectJsonNullDeadline() {
    long courseId = 1L;
    ProjectJson projectJson = new ProjectJson();
    projectJson.setName("Test Project");
    projectJson.setDescription("This is a test project.");
    projectJson.setMaxScore(100);
    projectJson.setGroupClusterId(1L);
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId)).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = projectUtil.checkProjectJson(projectJson, courseId);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void testCheckProjectDeadlinePast() {
    long courseId = 1L;
    ProjectJson projectJson = new ProjectJson();
    projectJson.setName("Test Project");
    projectJson.setDescription("This is a test project.");
    projectJson.setMaxScore(100);
    projectJson.setGroupClusterId(1L);
    projectJson.setDeadline(OffsetDateTime.now().minusDays(1));
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), courseId)).thenReturn(
        new CheckResult<>(HttpStatus.OK, "", null));
    CheckResult<Void> result = projectUtil.checkProjectJson(projectJson, courseId);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  public void testCanGetProject() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.admin);
    ProjectEntity projectEntity = new ProjectEntity();
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.of(projectEntity));
    when(projectRepository.userPartOfProject(projectId, user.getId())).thenReturn(true);
    when(projectRepository.adminOfProject(projectId, user.getId())).thenReturn(true);
    CheckResult<ProjectEntity> result = projectUtil.canGetProject(projectId, user);
    assertEquals(HttpStatus.OK, result.getStatus());
  }

  @Test
  public void testCanGetProjectForbidden() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.student);
    ProjectEntity projectEntity = new ProjectEntity();
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.of(projectEntity));
    when(projectRepository.userPartOfProject(projectId, user.getId())).thenReturn(false);
    when(projectRepository.adminOfProject(projectId, user.getId())).thenReturn(false);
    CheckResult<ProjectEntity> result = projectUtil.canGetProject(projectId, user);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());
  }

  @Test
  public void testCanGetProjectNotFound() {
    long projectId = 1L;
    UserEntity user = new UserEntity();
    user.setId(1L);
    user.setRole(UserRole.admin);
    when(projectRepository.findById(projectId)).thenReturn(java.util.Optional.empty());
    CheckResult<ProjectEntity> result = projectUtil.canGetProject(projectId, user);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }



}