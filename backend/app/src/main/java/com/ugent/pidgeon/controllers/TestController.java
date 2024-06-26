package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.json.TestJson;
import com.ugent.pidgeon.json.TestUpdateJson;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import com.ugent.pidgeon.util.CheckResult;
import com.ugent.pidgeon.util.CommonDatabaseActions;
import com.ugent.pidgeon.util.EntityToJsonConverter;
import com.ugent.pidgeon.util.FileUtil;
import com.ugent.pidgeon.util.Filehandler;
import com.ugent.pidgeon.util.Pair;
import com.ugent.pidgeon.util.ProjectUtil;
import com.ugent.pidgeon.util.TestUtil;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     * @param projectId the id of the project to update the tests for
     * @param auth the authentication object of the requesting user
     * @HttpMethod POST
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6697175">apiDog documentation</a>
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

    /**
     * Function to update the tests of a project
     * @param projectId the id of the project to update the tests for
     * @param auth the authentication object of the requesting user
     * @HttpMethod PATCH
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6693478">apiDog documentation</a>
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity with the updated tests
     */
    @PatchMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchTests(
        @RequestBody TestUpdateJson testJson,

        @PathVariable("projectid") long projectId,
        Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), testJson.getDockerImage(), testJson.getDockerScript(),
            testJson.getDockerTemplate(), testJson.getStructureTest(), HttpMethod.PATCH);
    }

    /**
     * Function to update the tests of a project
     * @param projectId the id of the project to update the tests for
     * @param auth the authentication object of the requesting user
     * @HttpMethod PUT
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724189">apiDog documentation</a>
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity with the updated tests
     */
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

        CheckResult<Pair<TestEntity, ProjectEntity>> updateCheckResult = testUtil.checkForTestUpdate(projectId, user, dockerImage, dockerScript, dockerTemplate, structureTemplate, httpMethod);


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
        if(dockerImage != null) {

          // update/install image if possible, do so in a seperate thread to reduce wait time.
          String finalDockerImage = dockerImage;
          CompletableFuture.runAsync(() -> {
              DockerSubmissionTestModel.installImage(finalDockerImage);
          });
        }

        String oldDockerImage = testEntity.getDockerImage();

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

      if (structureTemplate != null || !httpMethod.equals(HttpMethod.PATCH)) {
        testEntity.setStructureTemplate(structureTemplate);
      }
      // save test entity
      testEntity = testRepository.save(testEntity);
      projectEntity.setTestId(testEntity.getId());
      projectRepository.save(projectEntity); // make sure to update test id in project

      // Uninstall dockerimage if necessary
      if (oldDockerImage != null) {
        if (!testRepository.imageIsUsed(oldDockerImage)) {
          // Do it on a different thread
          String finalDockerImage1 = oldDockerImage;
          CompletableFuture.runAsync(() -> {
            DockerSubmissionTestModel.removeDockerImage(
                finalDockerImage1);
          });
        }
      }

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
        CheckResult<Pair<TestEntity, ProjectEntity>> updateCheckResult = testUtil.checkForTestUpdate(projectId, auth.getUserEntity(), null, null, null, null, HttpMethod.DELETE);
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

    /**
     * Function to upload extra files for a test
     * @param projectId the id of the project to upload the files for
     * @param file the file to upload
     * @param auth the authentication object of the requesting user
     * @HttpMethod PUT
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7409857">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/extrafiles
     * @return ResponseEntity with the updated tests
     */
    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/extrafiles")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> uploadExtraTestFiles(
        @PathVariable("projectid") long projectId,
        @RequestParam("file") MultipartFile file,
        Auth auth
    ) {
        CheckResult<TestEntity> checkResult = testUtil.getTestIfAdmin(projectId, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        TestEntity testEntity = checkResult.getData();

        try {
          Path path = Filehandler.getTestExtraFilesPath(projectId);
          Filehandler.saveFile(path, file, Filehandler.EXTRA_TESTFILES_FILENAME);

          FileEntity fileEntity = new FileEntity();
          fileEntity.setName(file.getOriginalFilename());
          fileEntity.setPath(path.resolve(Filehandler.EXTRA_TESTFILES_FILENAME).toString());
          fileEntity.setUploadedBy(auth.getUserEntity().getId());
          fileEntity = fileRepository.save(fileEntity);

          testEntity.setExtraFilesId(fileEntity.getId());
          testEntity = testRepository.save(testEntity);

          return ResponseEntity.ok(entityToJsonConverter.testEntityToTestJson(testEntity, projectId));
        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving files");
        }
    }

    /**
     * Function to delete extra files for a test
     * @param projectId the id of the project to delete the files for
     * @param auth the authentication object of the requesting user
     * @HttpMethod DELETE
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7409860">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/extrafiles
     * @return ResponseEntity with the updated tests
     */
    @DeleteMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/extrafiles")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteExtraTestFiles(
        @PathVariable("projectid") long projectId,
        Auth auth
    ) {
        CheckResult<TestEntity> checkResult = testUtil.getTestIfAdmin(projectId, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        TestEntity testEntity = checkResult.getData();

        try {

          FileEntity fileEntity = testEntity.getExtraFilesId() == null ?
              null : fileRepository.findById(testEntity.getExtraFilesId()).orElse(null);
          if (fileEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No extra files found");
          }

          testEntity.setExtraFilesId(null);
          testEntity = testRepository.save(testEntity);

          CheckResult<Void> delResult = fileUtil.deleteFileById(fileEntity.getId());
          if (!delResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(delResult.getStatus()).body(delResult.getMessage());
          }

          return ResponseEntity.ok(entityToJsonConverter.testEntityToTestJson(testEntity, projectId));
        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting files");
        }
    }

    /**
     * Function to get extra files for a test
     * @param projectId the id of the project to get the files for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-7409863">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/extrafiles
     * @return ResponseEntity with the updated tests
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/extrafiles")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getExtraTestFiles(
        @PathVariable("projectid") long projectId,
        Auth auth
    ) {
        CheckResult<TestEntity> checkResult = testUtil.getTestIfAdmin(projectId, auth.getUserEntity());
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }

        TestEntity testEntity = checkResult.getData();
        if (testEntity.getExtraFilesId() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No extra files found");
        }

        FileEntity fileEntity = fileRepository.findById(testEntity.getExtraFilesId()).orElse(null);
        if (fileEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No extra files found");
        }

        return Filehandler.getZipFileAsResponse(Path.of(fileEntity.getPath()), fileEntity.getName());
    }
}

