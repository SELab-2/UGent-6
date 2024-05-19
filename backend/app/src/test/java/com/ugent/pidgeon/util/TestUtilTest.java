package com.ugent.pidgeon.util;

import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUtilTest {

  @Mock
  private TestRepository testRepository;

  @Mock
  private ProjectUtil projectUtil;

  @Spy
  @InjectMocks
  private TestUtil testUtil;

  private TestEntity testEntity;
  private ProjectEntity projectEntity;
  private UserEntity userEntity;

  @BeforeEach
  public void setUp() {
    projectEntity = new ProjectEntity(
        99L,
        "projectName",
        "projectDescription",
        2L,
        100L,
        true,
        34,
        OffsetDateTime.now()
    );
    projectEntity.setId(64);
    userEntity = new UserEntity(
        "name",
        "surname",
        "email",
        UserRole.student,
        "azureId",
        ""
    );
    userEntity.setId(44L);
    testEntity = new TestEntity(
        "dockerImageBasic",
        "dockerTestScriptBasic",
        "dockerTestTemplateBasic",
        "structureTemplateBasic"
    );
    testEntity.setId(38L);
  }

  @Test
  public void testGetTestIfExists() {
    /* TestEntity exists */
    when(testRepository.findByProjectId(projectEntity.getId())).thenReturn(Optional.of(testEntity));
    assertEquals(testEntity, testUtil.getTestIfExists(projectEntity.getId()));

    /* TestEntity does not exist */
    when(testRepository.findByProjectId(projectEntity.getId())).thenReturn(Optional.empty());
    assertNull(testUtil.getTestIfExists(projectEntity.getId()));
  }

  @Test
  public void testCheckForTestUpdate() {
    String dockerImage = "dockerImage";
    String dockerScript = "dockerScript";
    String dockerTemplate = "@dockerTemplate\nExpectedOutput";
    String structureTemplate = "src/\n\tindex.js\n";
    HttpMethod httpMethod = HttpMethod.POST;

    when(projectUtil.getProjectIfAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", projectEntity));

    doReturn(testEntity).when(testUtil).getTestIfExists(projectEntity.getId());

    try (MockedStatic<DockerSubmissionTestModel> mockedTestModel = mockStatic(DockerSubmissionTestModel.class)) {
      mockedTestModel.when(() -> DockerSubmissionTestModel.imageExists(dockerImage)).thenReturn(true);
      mockedTestModel.when(() -> DockerSubmissionTestModel.isValidTemplate(any())).thenReturn(true);

      projectEntity.setTestId(null);
      CheckResult<Pair<TestEntity, ProjectEntity>> result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());
      assertEquals(testEntity, result.getData().getFirst());
      assertEquals(projectEntity, result.getData().getSecond());

      /* TestEntity not found and method is post */
      doReturn(null).when(testUtil).getTestIfExists(projectEntity.getId());
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          HttpMethod.POST
      );
      assertEquals(HttpStatus.OK, result.getStatus());
      doReturn(testEntity).when(testUtil).getTestIfExists(projectEntity.getId());


      /* Not a valid template */
      when(DockerSubmissionTestModel.isValidTemplate(any())).thenReturn(false);
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
      when(DockerSubmissionTestModel.isValidTemplate(any())).thenReturn(true);


      /* Method is patch and no template provided */
      projectEntity.setTestId(testEntity.getId());
      httpMethod = HttpMethod.PATCH;
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          null,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());

      /* Method is patch and script is null while test has a dockerImage */
      testEntity.setDockerTestScript(null);
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          null,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
      testEntity.setDockerTestScript(dockerScript);

      /* Method is patch and script is null but test already has a script */
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          null,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());

      /* Method is patch and image is null while test has a dockerScript */
      testEntity.setDockerImage(null);
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          null,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
      testEntity.setDockerImage(dockerImage);

      /* Method is patch and image is null but test already has an image */
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          null,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());

      /* Patch method with everything present in request, nothing in test */
      testEntity.setDockerImage(null);
      testEntity.setDockerTestScript(null);
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());
      testEntity.setDockerImage(dockerImage);
      testEntity.setDockerTestScript(dockerScript);

      /* Method not patch and template provided without script */
      httpMethod = HttpMethod.PUT;
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          null,
          null,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

      /* Method not patch and no args provided */
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          null,
          null,
          null,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());

      /* Invalid dockerImage */
      when(DockerSubmissionTestModel.imageExists(dockerImage)).thenReturn(false);
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
      when(DockerSubmissionTestModel.imageExists(dockerImage)).thenReturn(true);

      /* dockerImage without script */
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          null,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

      /* dockerScript without image */
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          null,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

      /* Method is post and test already exists */
      projectEntity.setTestId(99L);
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          HttpMethod.POST
      );
      assertEquals(HttpStatus.CONFLICT, result.getStatus());

      /* Method is delete and test is found */
      httpMethod = HttpMethod.DELETE;
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          null,
          null,
          null,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.OK, result.getStatus());

      /* TestEntity not found and method is not post */
      doReturn(null).when(testUtil).getTestIfExists(projectEntity.getId());
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          HttpMethod.PATCH
      );
      assertEquals(HttpStatus.NOT_FOUND, result.getStatus());


      /* Project check fails */
      when(projectUtil.getProjectIfAdmin(projectEntity.getId(), userEntity))
          .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Project not found", null));
      result = testUtil.checkForTestUpdate(
          projectEntity.getId(),
          userEntity,
          dockerImage,
          dockerScript,
          dockerTemplate,
          structureTemplate,
          httpMethod
      );
      assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    }
  }

  @Test
  public void testGetTestIfAdmin() {
    /* TestEntity exists */
    doReturn(testEntity).when(testUtil).getTestIfExists(projectEntity.getId());
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    CheckResult<TestEntity> result = testUtil.getTestIfAdmin(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());

    /* User not admin */
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "User is not an admin", null));
    result = testUtil.getTestIfAdmin(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* TestEntity not found */
    doReturn(null).when(testUtil).getTestIfExists(projectEntity.getId());
    result = testUtil.getTestIfAdmin(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());

  }

  @Test
  public void testGetTestWithAdminStatus() {
    doReturn(testEntity).when(testUtil).getTestIfExists(projectEntity.getId());
    when(projectUtil.userPartOfProject(projectEntity.getId(), userEntity.getId())).thenReturn(true);
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.OK, "", null));

    CheckResult<Pair<TestEntity, Boolean>> result = testUtil.getTestWithAdminStatus(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertTrue(result.getData().getSecond());

    /* User not admin */
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.FORBIDDEN, "User is not an admin", null));
    result = testUtil.getTestWithAdminStatus(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertFalse(result.getData().getSecond());

    /* User not admin but general admin */
    userEntity.setRole(UserRole.admin);
    result = testUtil.getTestWithAdminStatus(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.OK, result.getStatus());
    assertTrue(result.getData().getSecond());

    /* Project admin check returns unexpected status */
    when(projectUtil.isProjectAdmin(projectEntity.getId(), userEntity))
        .thenReturn(new CheckResult<>(HttpStatus.I_AM_A_TEAPOT, "Unexpected error", null));
    result = testUtil.getTestWithAdminStatus(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.I_AM_A_TEAPOT, result.getStatus());

    /* User not part of project */
    when(projectUtil.userPartOfProject(projectEntity.getId(), userEntity.getId())).thenReturn(false);
    result = testUtil.getTestWithAdminStatus(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatus());

    /* TestEntity not found */
    doReturn(null).when(testUtil).getTestIfExists(projectEntity.getId());
    result = testUtil.getTestWithAdminStatus(projectEntity.getId(), userEntity);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
  }


}