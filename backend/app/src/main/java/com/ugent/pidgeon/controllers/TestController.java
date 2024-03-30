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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
     * @HttpMethod PUT
     * @ApiDog <a href="https://apidog.com/apidoc/project-467959/api-5724189">apiDog documentation</a>
     * @AllowedRoles teacher
     * @ApiPath /api/projects/{projectid}/tests
     * @return ResponseEntity with the updated tests
     */
    @PutMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher})
    public ResponseEntity<Object> updateTests(
            @RequestParam("dockerimage") String dockerImage,
            @RequestParam("dockertest") MultipartFile dockerTest,
            @RequestParam("structuretest") MultipartFile structureTest,
            @PathVariable("projectid") long projectId,
            Auth auth) {

        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        long userId = auth.getUserEntity().getId();
        if(!projectRepository.adminOfProject(projectId, userId) && auth.getUserEntity().getRole() != UserRole.admin){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        try {
            // Save the files on server
            Path dockerTestPath = Filehandler.saveTest(dockerTest, projectId);
            Path structureTestPath = Filehandler.saveTest(structureTest, projectId);

            // Save file entities to the database
            FileEntity dockertestFileEntity = saveFileEntity(dockerTestPath, projectId, userId);
            FileEntity structuretestFileEntity = saveFileEntity(structureTestPath, projectId, userId);

            // Create/update test entity
            Optional<TestEntity> testEntity = testRepository.findByProjectId(projectId);
            TestEntity test;
            if (testEntity.isEmpty()) {
                TestEntity newTestEntity = new TestEntity(dockerImage, dockertestFileEntity.getId(), structuretestFileEntity.getId());

                test = testRepository.save(newTestEntity);
                projectEntity.setTestId(test.getId());
                // Update project entity because first time test is created so id is not set
                projectRepository.save(projectEntity);
            } else {
                TestEntity newTestEntity = testEntity.get();
                newTestEntity.setDockerImage(dockerImage);
                newTestEntity.setDockerTestId(dockertestFileEntity.getId());
                newTestEntity.setStructureTestId(structuretestFileEntity.getId());
                test = testRepository.save(newTestEntity);
            }



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
    @Roles({UserRole.teacher})
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
            Permission permission = PermissionHandler.userIsCouresAdmin(courseUserEntity.get());
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

