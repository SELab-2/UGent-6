package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.TestJson;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.util.Filehandler;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
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
import java.util.logging.FileHandler;
import java.util.logging.Logger;

@RestController
public class TestController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TestRepository testRepository;

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
        if(!projectRepository.adminOfProject(projectId, userId)){
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
                newTestEntity.setDockerTest(dockertestFileEntity.getId());
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

    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getTests(@PathVariable("projectid") long projectId, Auth auth) {
        long userId = auth.getUserEntity().getId();
        if (!projectRepository.adminOfProject(projectId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        Optional<TestEntity> testEntity = testRepository.findByProjectId(projectId);
        if (testEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
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

    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/structuretest")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getStructureTestFile(@PathVariable("projectid") long projectId, Auth auth) {
        long userId = auth.getUserEntity().getId();
        if (!projectRepository.adminOfProject(projectId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        Optional<ProjectEntity> projectEntity = projectRepository.findById(projectId);
        if (projectEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        long testId = projectEntity.get().getTestId();
        Optional<TestEntity> testEntity = testRepository.findById(testId);
        if (testEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        long structuretestid = testEntity.get().getStructureTestId();
        Optional<FileEntity> fileEntity = fileRepository.findById(structuretestid);
        if (fileEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resource file = Filehandler.getFileAsResource(Path.of(fileEntity.get().getPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.get().getName());
        headers.add(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.TEXT_PLAIN));
        return ResponseEntity.ok().headers(headers).body(file);
    }

    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/tests/dockertest")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Object> getDockerTestFile(@PathVariable("projectid") long projectId, Auth auth) {
        long userId = auth.getUserEntity().getId();
        if (!projectRepository.adminOfProject(projectId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        Optional<ProjectEntity> projectEntity = projectRepository.findById(projectId);
        if (projectEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        long testId = projectEntity.get().getTestId();
        Optional<TestEntity> testEntity = testRepository.findById(testId);
        if (testEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        long dockertestid = testEntity.get().getDockerTest();
        Optional<FileEntity> fileEntity = fileRepository.findById(dockertestid);
        if (fileEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resource file = Filehandler.getFileAsResource(Path.of(fileEntity.get().getPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileEntity.get().getName());
        headers.add(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.TEXT_PLAIN));
        return ResponseEntity.ok().headers(headers).body(file);
    }

}

