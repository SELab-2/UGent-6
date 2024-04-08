package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.postgre.models.*;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.Filehandler;
import com.ugent.pidgeon.util.Permission;
import com.ugent.pidgeon.util.PermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Path;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

@RestController
public class TestController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private FileController fileController;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseUserRepository courseUserRepository;

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
        ProjectEntity projectEntity = projectRepository.findById(projectId).orElse(null);
        if (projectEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
        }

        if(!projectRepository.adminOfProject(projectId, user.getId()) && user.getRole() != UserRole.admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have acces to update the tests of this project");
        }

        if (httpMethod.equals(HttpMethod.POST) && projectEntity.getTestId() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tests already exist for this project");
        }

        if (!httpMethod.equals(HttpMethod.PATCH)) {
            if (dockerImage == null || dockerTest == null || structureTest == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing parameters: dockerimage (string), dockertest (file), structuretest (file) are required");
            }
        }

        try {
            // Get test entity
            Optional<TestEntity> testEntityOptional = testRepository.findByProjectId(projectId);
            if (testEntityOptional.isEmpty() && !httpMethod.equals(HttpMethod.POST)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tests found for project with id: " + projectId);
            }
            // Save the files on server
            Long dockertestFileEntityId;
            Long structuretestFileEntityId;
            if (dockerTest != null) {
                Path dockerTestPath = Filehandler.saveTest(dockerTest, projectId);
                FileEntity dockertestFileEntity = saveFileEntity(dockerTestPath, projectId, user.getId());
                dockertestFileEntityId = dockertestFileEntity.getId();
            } else {
                dockertestFileEntityId = testEntityOptional.get().getDockerTestId();
            }

            if (structureTest != null) {
                Path structureTestPath = Filehandler.saveTest(structureTest, projectId);
                FileEntity structuretestFileEntity = saveFileEntity(structureTestPath, projectId, user.getId());
                structuretestFileEntityId = structuretestFileEntity.getId();
            } else {
                structuretestFileEntityId = testEntityOptional.get().getStructureTestId();
            }

            // Create/update test entity
            TestEntity test = new TestEntity(dockerImage, dockertestFileEntityId, structuretestFileEntityId);
            test = testRepository.save(test);
            projectEntity.setTestId(test.getId());
            projectRepository.save(projectEntity);
            return ResponseEntity.ok(entityToTestJson(test, projectId));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving files: " + e.getMessage());
        }
    }

    // Hulpfunctie om de tests correct op de database te zetten
    private FileEntity saveFileEntity(Path filePath, long projectId, long userId) throws IOException {
        // Save the file entity to the database
        Logger.getGlobal().info("file path: " + filePath.toString());
        Logger.getGlobal().info("file name: " + filePath.getFileName().toString());
        FileEntity fileEntity = new FileEntity(filePath.getFileName().toString(), filePath.toString(), userId);
        return fileRepository.save(fileEntity);
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
        long userId = auth.getUserEntity().getId();
        if (!projectRepository.adminOfProject(projectId, userId) && auth.getUserEntity().getRole() != UserRole.admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        Optional<TestEntity> testEntity = testRepository.findByProjectId(projectId);
        if (testEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tests found for project with id: " + projectId);
        }
        TestEntity test = testEntity.get();
        TestJson res  = entityToTestJson(test, projectId);
        return ResponseEntity.ok(res);
    }


    public TestJson entityToTestJson(TestEntity testEntity, long projectId) {
        return new TestJson(
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId,
                testEntity.getDockerImage(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/dockertest",
                ApiRoutes.PROJECT_BASE_PATH + "/" + projectId + "/tests/structuretest"
        );
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
        long userId = auth.getUserEntity().getId();
        if (!projectRepository.adminOfProject(projectId, userId) && auth.getUserEntity().getRole() != UserRole.admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        Optional<ProjectEntity> projectEntity = projectRepository.findById(projectId);
        if (projectEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found with id: " + projectId);
        }
        long testId = projectEntity.get().getTestId();
        Optional<TestEntity> testEntity = testRepository.findById(testId);
        if (testEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tests found for project with id: " + projectId);
        }
        long testFileId = testFileIdGetter.apply(testEntity.get());
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
     * @param testId the id of the test to delete
     * @param auth the authentication object of the requesting user
     * @HttpMethod DELETE
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724189">apiDog documentation</a>
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity
     */
    @DeleteMapping(ApiRoutes.TEST_BASE_PATH + "/{testId}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> deleteTestById(@PathVariable("testId") long testId, Auth auth) {
        // Get the submission entry from the database
        TestEntity testEntity = testRepository.findById(testId).orElse(null);
        if (testEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        CourseEntity courseEntity = courseRepository.findCourseEntityByTestId(testId).get(0);
        if (courseEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found");
        }
        if(auth.getUserEntity().getRole()!= UserRole.admin) {
            Optional<CourseUserEntity> courseUserEntity = courseUserRepository.findByCourseIdAndUserId(courseEntity.getId(), auth.getUserEntity().getId());
            if (courseUserEntity.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in course");
            }
            Permission permission = PermissionHandler.userIsCourseAdmin(courseUserEntity.get());
            if (!permission.hasPermission()) {
                return permission.getResponseEntity();
            }
        }
        testRepository.delete(testEntity);
        fileController.deleteFileById(testEntity.getStructureTestId());
        fileController.deleteFileById(testEntity.getDockerTestId());
        return  ResponseEntity.ok().build();
    }
}

