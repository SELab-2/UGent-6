package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
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
            @RequestParam(name = "dockertest", required = false) MultipartFile dockerTest,
            @RequestParam(name = "structuretest", required = false) MultipartFile structureTest,
            @PathVariable("projectid") long projectId,
            Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), dockerImage, dockerTest, structureTest, HttpMethod.POST);
    }

    @PatchMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> patchTests(
            @RequestParam(name = "dockerimage", required = false) String dockerImage,
            @RequestParam(name = "dockertest", required = false) MultipartFile dockerTest,
            @RequestParam(name = "structuretest", required = false) MultipartFile structureTest,
            @PathVariable("projectid") long projectId,
            Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), dockerImage, dockerTest, structureTest, HttpMethod.PATCH);
    }

    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> putTests(
            @RequestParam(name = "dockerimage", required = false) String dockerImage,
            @RequestParam(name = "dockertest", required = false) MultipartFile dockerTest,
            @RequestParam(name = "structuretest", required = false) MultipartFile structureTest,
            @PathVariable("projectid") long projectId,
            Auth auth) {
        return alterTests(projectId, auth.getUserEntity(), dockerImage, dockerTest, structureTest, HttpMethod.PUT);
    }


    private ResponseEntity<?> alterTests(
            long projectId,
            UserEntity user,
            String dockerImage,
            MultipartFile dockerTest,
            MultipartFile structureTest,
            HttpMethod httpMethod
    ) {

        CheckResult<Pair<TestEntity, ProjectEntity>> checkResult = testUtil.checkForTestUpdate(projectId, user, dockerImage, dockerTest, structureTest, httpMethod);
        if (!checkResult.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(checkResult.getStatus()).body(checkResult.getMessage());
        }
        TestEntity testEntity = checkResult.getData().getFirst();
        ProjectEntity projectEntity = checkResult.getData().getSecond();

        try {

            // Save the files on server
            long dockertestFileEntityId;
            long structuretestFileEntityId;
            if (dockerTest != null) {
                Path dockerTestPath = Filehandler.saveTest(dockerTest, projectId);
                FileEntity dockertestFileEntity = fileUtil.saveFileEntity(dockerTestPath, projectId, user.getId());
                dockertestFileEntityId = dockertestFileEntity.getId();
            } else {
                dockertestFileEntityId = testEntity.getDockerTestId();
            }

            if (structureTest != null) {
                Path structureTestPath = Filehandler.saveTest(structureTest, projectId);
                FileEntity structuretestFileEntity = fileUtil.saveFileEntity(structureTestPath, projectId, user.getId());
                structuretestFileEntityId = structuretestFileEntity.getId();
            } else {
                structuretestFileEntityId = testEntity.getStructureTestId();
            }

            // Create/update test entity
            TestEntity test = new TestEntity(dockerImage, dockertestFileEntityId, structuretestFileEntityId);
            test = testRepository.save(test);
            projectEntity.setTestId(test.getId());
            projectRepository.save(projectEntity);
            return ResponseEntity.ok(testUtil.testEntityToTestJson(test, projectId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving files: " + e.getMessage());
        }
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
        TestJson res  = testUtil.testEntityToTestJson(test, projectId);
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
        return getTestFileResponseEnity(projectId, auth, TestEntity::getStructureTestId);
    }

    /**
     * Function to get the docker test file of a project
     * @param projectId the id of the project to get the docker test file for
     * @param auth the authentication object of the requesting user
     * @HttpMethod GET
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-6133798">apiDog documentation</a>
     * @AllowedRoles teacher, student
     * @ApiPath /api/projects/{projectid}/tests/dockertest
     * @return ResponseEntity with the docker test file
     */
    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/dockertest")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getDockerTestFile(@PathVariable("projectid") long projectId, Auth auth) {
        return getTestFileResponseEnity(projectId, auth, TestEntity::getDockerTestId);
    }

    public ResponseEntity<?> getTestFileResponseEnity(long projectId, Auth auth, Function<TestEntity, Long> testFileIdGetter) {
        CheckResult<TestEntity> projectCheck = testUtil.getTestIfAdmin(projectId, auth.getUserEntity());
        if (!projectCheck.getStatus().equals(HttpStatus.OK)) {
            return ResponseEntity.status(projectCheck.getStatus()).body(projectCheck.getMessage());
        }
        TestEntity testEntity = projectCheck.getData();

        long testFileId = testFileIdGetter.apply(testEntity);
        Optional<FileEntity> fileEntity = fileRepository.findById(testFileId);
        if (fileEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No file found for test with id: " + testFileId);
        }
        Resource file = Filehandler.getFileAsResource(Path.of(fileEntity.get().getPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.get().getName());
        headers.add(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.TEXT_PLAIN));
        return ResponseEntity.ok().headers(headers).body(file);
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
        CheckResult<Pair<TestEntity, ProjectEntity>> updateCheckResult = testUtil.checkForTestUpdate(projectId, auth.getUserEntity(), null, null, null, HttpMethod.DELETE);
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

