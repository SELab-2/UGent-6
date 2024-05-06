package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
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
        @RequestParam(name = "dockerimage", required = false) String dockerImage,
        @RequestParam(name = "dockerscript", required = false) String dockerTest,
        @RequestParam(name = "dockertemplate", required = false) String dockerTemplate,
        @RequestParam(name = "structuretest", required = false) String structureTest,
        @PathVariable("projectid") long projectId,
        Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), dockerImage, dockerTest, structureTest, dockerTemplate, HttpMethod.POST);
    }

    @PatchMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchTests(
        @RequestParam(name = "dockerimage", required = false) String dockerImage,
        @RequestParam(name = "dockerscript", required = false) String dockerTest,
        @RequestParam(name = "dockertemplate", required = false) String dockerTemplate,
        @RequestParam(name = "structuretest", required = false) String structureTest,
        @PathVariable("projectid") long projectId,
        Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), dockerImage, dockerTest, structureTest, dockerTemplate, HttpMethod.PATCH);
    }

    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> putTests(
            @RequestParam(name = "dockerimage", required = false) String dockerImage,
            @RequestParam(name = "dockerscript", required = false) String dockerTest,
            @RequestParam(name = "dockertemplate", required = false) String dockerTemplate,
            @RequestParam(name = "structuretest", required = false) String structureTest,
            @PathVariable("projectid") long projectId,
            Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), dockerImage, dockerTest, structureTest, dockerTemplate, HttpMethod.PUT);
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

        CheckResult<Pair<TestEntity, ProjectEntity>> updateCheckResult = testUtil.checkForTestUpdate(projectId, user, dockerImage, null, null, httpMethod);


        if (!updateCheckResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(updateCheckResult.getStatus()).body(updateCheckResult.getMessage());
        }

        TestEntity testEntity = updateCheckResult.getData().getFirst();
        ProjectEntity projectEntity = updateCheckResult.getData().getSecond();

        // Creating a test entry
        if(httpMethod.equals(HttpMethod.POST)){
            testEntity = new TestEntity();
        }

        // delete test entry
        if(httpMethod.equals(HttpMethod.DELETE)){
            // first check if docker image is not used anywhere else
            if(!testRepository.imageIsUsed(dockerImage)){
                // image is no longer required for any tests
                DockerSubmissionTestModel.removeDockerImage(dockerImage);
            }

            // delete test
            testRepository.deleteById(testEntity.getId());
            projectEntity.setTestId(null);
            projectRepository.save(projectEntity);
            return ResponseEntity.ok().build();
        }

        // Docker test
        if(dockerImage != null){
            // update/install image if possible.
            DockerSubmissionTestModel.installImage(dockerImage);
            testEntity.setDockerImage(dockerImage);

            testEntity.setDockerTestScript(dockerScript);
            testEntity.setDockerTestTemplate(dockerTemplate); // If present, the test is in template mode
        }

        // save structure template
        if (!httpMethod.equals(HttpMethod.PATCH) || (structureTemplate != null && !structureTemplate.isBlank())) {
            if (structureTemplate != null && structureTemplate.isBlank()) {
                structureTemplate = null;
            }
        } else {
            structureTemplate = testEntity.getStructureTemplate();
        }
        testEntity.setStructureTemplate(structureTemplate);

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
        CheckResult<TestEntity> projectCheck = testUtil.getTestIfAdmin(projectId, auth.getUserEntity());
        if (!projectCheck.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(projectCheck.getStatus()).body(projectCheck.getMessage());
        }
        TestEntity test = projectCheck.getData();
        TestJson res  = entityToJsonConverter.testEntityToTestJson(test, projectId);
        return ResponseEntity.ok(res);
    }

    /**
     * Function to get the structure test file of a project
     * @param projectId the id of the project to get the structure test file for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6133750">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/structuretest
     * @return ResponseEntity with the structure test file
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/structuretest")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getStructureTestFile(@PathVariable("projectid") long projectId, Auth auth) {
        return getTestProperty(projectId, auth, TestEntity::getStructureTemplate);
    }

    /**
     * Function to get the docker test template of a project
     * @param projectId the id of the project to get the docker test file for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6133798">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/dockertest
     * @return ResponseEntity with the docker test file
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/dockertesttemplate")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getDockerTestTemplate(@PathVariable("projectid") long projectId, Auth auth) {
        return getTestProperty(projectId, auth, TestEntity::getDockerTestTemplate);
    }

    /**
     * Function to get the docker test script of a project
     * @param projectId the id of the project to get the docker test file for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6133798">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/dockertest
     * @return ResponseEntity with the docker test file
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/dockertestscript")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getDockerTestScript(@PathVariable("projectid") long projectId, Auth auth) {
        return getTestPropertyCheckAdmin(projectId, auth, TestEntity::getDockerTestScript);
    }

    /**
     * Function to get the docker image of a project test
     * @param projectId the id of the project to get the docker test file for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6133798">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/dockertest
     * @return ResponseEntity with the docker test file
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/dockertestimage")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getDockerTestImage(@PathVariable("projectid") long projectId, Auth auth) {
        return getTestPropertyCheckAdmin(projectId, auth, TestEntity::getDockerImage);
    }


    public ResponseEntity<?> getTestPropertyCheckAdmin(long projectId, Auth auth, Function<TestEntity, String> propertyGetter) {
        CheckResult<TestEntity> projectCheck = testUtil.getTestIfAdmin(projectId, auth.getUserEntity());
        if (!projectCheck.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(projectCheck.getStatus()).body(projectCheck.getMessage());
        }
        TestEntity testEntity = projectCheck.getData();
        return ResponseEntity.ok(propertyGetter.apply(testEntity));
    }
    public ResponseEntity<?> getTestProperty(long projectId, Auth auth, Function<TestEntity, String> propertyGetter) {
        TestEntity testEntity = testUtil.getTestIfExists(projectId);
        if (testEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tests found for project with id: " + projectId);
        }

        return propertyGetter.apply(testEntity) == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No test found") : ResponseEntity.ok(propertyGetter.apply(testEntity));
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

