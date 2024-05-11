package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.model.json.TestUpdateJson;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

import java.util.Optional;
import java.util.function.Function;

@RestController
public class TestController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TestUtil testUtil;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private CommonDatabaseActions commonDatabaseActions;
    @Autowired
    private EntityToJsonConverter entityToJsonConverter;
  @Autowired
  private ProjectUtil projectUtil;

    /**
     * Function to update the tests of a project
     * @param dockerImage the docker image to use for the tests
     * @param dockerTest the docker test file
     * @param structureTest the structure test file
     * @param projectId the id of the project to update the tests for
     * @param auth the authentication object of the requesting user
     * @HttpMethod POST
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724189">apiDog documentation</a>
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity with the updated tests
     */
    @PostMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> updateTests(
        @RequestBody TestUpdateJson testJson,
        @PathVariable("projectid") long projectId,
        Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), testJson.getDockerImage(), testJson.getDockerScript(),
            testJson.getDockerTemplate(), testJson.getStructureTest(), HttpMethod.POST);
    }

    @PatchMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchTests(
        @RequestBody TestUpdateJson testJson,
        @PathVariable("projectid") long projectId,
        Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), testJson.getDockerImage(), testJson.getDockerScript(),
            testJson.getDockerTemplate(), testJson.getStructureTest(), HttpMethod.PATCH);
    }

    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> putTests(
            @RequestBody TestUpdateJson testJson,
            @PathVariable("projectid") long projectId,
            Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), testJson.getDockerImage(), testJson.getDockerScript(),
            testJson.getDockerTemplate(), testJson.getStructureTest(), HttpMethod.PUT);
    }


    private ResponseEntity<?> alterTests(
            long projectId,
            UserEntity user,
            String dockerImage,
            String dockerScript,
            String dockerTemplate,
            String structureTemplate,
            HttpMethod httpMethod
    ) {




        if (dockerImage != null && dockerImage.isBlank()) {
            dockerImage = null;
        }
        if (dockerScript != null && dockerScript.isBlank()) {
            dockerScript = null;
        }
        if (dockerTemplate != null && dockerTemplate.isBlank()) {
            dockerTemplate = null;
        }
        if (structureTemplate != null && structureTemplate.isBlank()) {
            structureTemplate = null;
        }

      /* LOg arguments even if null */
      System.out.println("dockerImage: " + dockerImage);
      System.out.println("dockerScript: " + dockerScript);
      System.out.println("dockerTemplate: " + dockerTemplate);
      System.out.println("structureTemplate: " + structureTemplate);

        CheckResult<Pair<TestEntity, ProjectEntity>> updateCheckResult = testUtil.checkForTestUpdate(projectId, user, dockerImage, dockerScript, dockerTemplate, httpMethod);


        if (!updateCheckResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(updateCheckResult.getStatus()).body(updateCheckResult.getMessage());
        }

        TestEntity testEntity = updateCheckResult.getData().getFirst();
        ProjectEntity projectEntity = updateCheckResult.getData().getSecond();

        // Creating a test entry
        if(httpMethod.equals(HttpMethod.POST)){
            testEntity = new TestEntity();
        }

        // Docker test
        if(!(dockerImage == null && dockerScript == null && dockerTemplate == null)) {

          // update/install image if possible, do so in a seperate thread to reduce wait time.
          String finalDockerImage = dockerImage;
          CompletableFuture.runAsync(() -> {
            if (finalDockerImage != null) {
              DockerSubmissionTestModel.installImage(finalDockerImage);
            }
          });

          //Update fields
          if (dockerImage != null || !httpMethod.equals(HttpMethod.PATCH)) {
            testEntity.setDockerImage(dockerImage);
            if (!testRepository.imageIsUsed(dockerImage)) {
              // Do it on a different thread
              String finalDockerImage1 = dockerImage;
              CompletableFuture.runAsync(() -> {
                  DockerSubmissionTestModel.removeDockerImage(
                      finalDockerImage1);
              });
            }
          }

          if (dockerScript != null || !httpMethod.equals(HttpMethod.PATCH)) {
            testEntity.setDockerTestScript(dockerScript);
          }
          if (dockerTemplate != null || !httpMethod.equals(HttpMethod.PATCH)) {
            testEntity.setDockerTestTemplate(dockerTemplate);
          }
        }

      if (structureTemplate != null || !httpMethod.equals(HttpMethod.PATCH)) {
        testEntity.setStructureTemplate(structureTemplate);
      }
      // save test entity
      testEntity = testRepository.save(testEntity);
      projectEntity.setTestId(testEntity.getId());
      projectRepository.save(projectEntity); // make sure to update test id in project

      return ResponseEntity.ok(entityToJsonConverter.testEntityToTestJson(testEntity, projectId));

    }



    /**
     * Function to get the tests of a project
     * @param projectId the id of the project to get the tests for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724035">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity with the tests of the project
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getTests(@PathVariable("projectid") long projectId, Auth auth) {
        CheckResult<Pair<TestEntity, Boolean>> projectCheck = testUtil.getTestWithAdminStatus(projectId, auth.getUserEntity());
        if (!projectCheck.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(projectCheck.getStatus()).body(projectCheck.getMessage());
        }
        TestEntity test = projectCheck.getData().getFirst();
        if (!projectCheck.getData().getSecond()) { // user is not an admin, hide script and image
          test.setDockerTestScript(null);
          test.setDockerImage(null);
        }
        TestJson res  = entityToJsonConverter.testEntityToTestJson(test, projectId);
        return ResponseEntity.ok(res);
    }

    /**
     * Function to delete the tests of a project
     * @param projectId the id of the test to delete
     * @param auth the authentication object of the requesting user
     * @HttpMethod DELETE
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724189">apiDog documentation</a>
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity
     */
    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteTestById(@PathVariable("projectid") long projectId, Auth auth) {
        CheckResult<Pair<TestEntity, ProjectEntity>> updateCheckResult = testUtil.checkForTestUpdate(projectId, auth.getUserEntity(), null, null, null,  HttpMethod.DELETE);
        if (!updateCheckResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(updateCheckResult.getStatus()).body(updateCheckResult.getMessage());
        }

        ProjectEntity projectEntity = updateCheckResult.getData().getSecond();
        TestEntity testEntity = updateCheckResult.getData().getFirst();

        CheckResult<Void> deleteResult = commonDatabaseActions.deleteTestById(projectEntity, testEntity);
        if (!deleteResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(deleteResult.getStatus()).body(deleteResult.getMessage());
        }
        return  ResponseEntity.ok().build();
    }
}

