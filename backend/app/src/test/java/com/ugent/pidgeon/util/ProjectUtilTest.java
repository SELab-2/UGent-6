package com.ugent.pidgeon.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.ugent.pidgeon.json.ProjectJson;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class ProjectUtilTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ClusterUtil clusterUtil;

  @Spy
  @InjectMocks
  private ProjectUtil projectUtil;

  private ProjectEntity projectEntity;
  private UserEntity mockUser;

  @BeforeEach
  public void setUp() {
    projectEntity = new ProjectEntity(
        99L,
        "projectName",
        "projectDescription",
        69L,
        38L,
        true,
        34,
        OffsetDateTime.now()
    );
    projectEntity.setId(64);

    mockUser = new UserEntity("name", "surname", "email", UserRole.student, "azureid", "");
    mockUser.setId(10L);
  }

  @Test
  public void testUserPartOfProject() {
    /* User in project */
    when(projectRepository.userPartOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(true);
    assertTrue(projectUtil.userPartOfProject(projectEntity.getId(), mockUser.getId()));

    /* User not in project */
    when(projectRepository.userPartOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(false);
    assertFalse(projectUtil.userPartOfProject(projectEntity.getId(), mockUser.getId()));
  }


  @Test
  public void testGetProjectIfExists() {
    /* Project found */
    when(projectRepository.findById(projectEntity.getId())).thenReturn(java.util.Optional.of(projectEntity));
    CheckResult<ProjectEntity> checkResult = projectUtil.getProjectIfExists(projectEntity.getId());
    assertEquals(HttpStatus.OK, checkResult.getStatus());
    assertEquals(projectEntity, checkResult.getData());

    /* Project not found */
    when(projectRepository.findById(projectEntity.getId())).thenReturn(java.util.Optional.empty());
    checkResult = projectUtil.getProjectIfExists(projectEntity.getId());
    assertEquals(HttpStatus.NOT_FOUND, checkResult.getStatus());
  }

  @Test
  public void testIsProjectAdmin() {
    /* User is admin */
    when(projectRepository.adminOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(true);
    CheckResult<Void> checkResult = projectUtil.isProjectAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* User is not admin */
    when(projectRepository.adminOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(false);
    checkResult = projectUtil.isProjectAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, checkResult.getStatus());

    /* User is general admin */
    mockUser.setRole(UserRole.admin);
    checkResult = projectUtil.isProjectAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, checkResult.getStatus());
  }

  @Test
  public void testGetProjectIfAdmin() {
    /* All checks succeed */
    doReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity)).when(projectUtil).getProjectIfExists(projectEntity.getId());
    when(projectRepository.adminOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(true);

    CheckResult<ProjectEntity> checkResult = projectUtil.getProjectIfAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* User is not admin */
    when(projectRepository.adminOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(false);
    checkResult = projectUtil.getProjectIfAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, checkResult.getStatus());

    /* User is not project admin but admin role */
    mockUser.setRole(UserRole.admin);
    checkResult = projectUtil.getProjectIfAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* Project not found */
    doReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "Project not found", null)).when(projectUtil).getProjectIfExists(projectEntity.getId());
    checkResult = projectUtil.getProjectIfAdmin(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, checkResult.getStatus());
  }

  @Test
  public void testCheckProjectJson() {
    ProjectJson projectJson = new ProjectJson(
        "UpdateProjectName",
        "UpdateProjectDescription",
        69L,
        true,
        34,
        OffsetDateTime.now().plusDays(1)
    );

    /* All checks succeed */
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), projectEntity.getCourseId()))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    CheckResult<Void> checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* projectJson maxScore is negative */
    projectJson.setMaxScore(-1);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* projectJson maxScore is zero */
    projectJson.setMaxScore(0);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* projectJson no max score */
    projectJson.setMaxScore(null);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* projectJson deadline is already passed */
    projectJson.setDeadline(OffsetDateTime.now().minusDays(1));
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* Cluster not part of course */
    when(clusterUtil.partOfCourse(projectJson.getGroupClusterId(), projectEntity.getCourseId()))
        .thenReturn(new CheckResult<>(HttpStatus.NOT_FOUND, "Cluster not part of course", null));
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.NOT_FOUND, checkResult.getStatus());

    /* name is blank */
    projectJson.setName("");
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* deadline is null */
    projectJson.setDeadline(null);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* groupClusterId is null */
    projectJson.setDeadline(OffsetDateTime.now().plusDays(1));
    projectJson.setGroupClusterId(null);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* description is null */
    projectJson.setDescription(null);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());

    /* name is null */
    projectJson.setName(null);
    checkResult = projectUtil.checkProjectJson(projectJson, projectEntity.getCourseId());
    assertEquals(HttpStatus.BAD_REQUEST, checkResult.getStatus());
  }

  @Test
  public void testCanGetProject() {
    /* User is student */
    when(projectRepository.findById(projectEntity.getId())).thenReturn(java.util.Optional.of(projectEntity));
    when(projectRepository.userPartOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(true);

    CheckResult<ProjectEntity> checkResult = projectUtil.canGetProject(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* User is admin */
    when(projectRepository.userPartOfProject(projectEntity.getId(), mockUser.getId())).thenReturn(false);
    mockUser.setRole(UserRole.admin);
    checkResult = projectUtil.canGetProject(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.OK, checkResult.getStatus());

    /* User is not part of project */
    mockUser.setRole(UserRole.student);
    checkResult = projectUtil.canGetProject(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.FORBIDDEN, checkResult.getStatus());

    /* Project not found */
    when(projectRepository.findById(projectEntity.getId())).thenReturn(java.util.Optional.empty());
    checkResult = projectUtil.canGetProject(projectEntity.getId(), mockUser);
    assertEquals(HttpStatus.NOT_FOUND, checkResult.getStatus());
  }

}