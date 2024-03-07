package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@RestController
public class JpaProjectTestController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TestRepository testRepository;

    @PutMapping("/project/{projectid}/tests")
    public ResponseEntity<String> updateTests(
            @RequestParam("dockerimage") MultipartFile dockerImage,
            @RequestParam("dockertest") MultipartFile dockerTest,
            @RequestParam("structuretest") MultipartFile structureTest,
            @PathVariable("projectid") long projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        long userId = 1L; //TODO: replace with id of current user
        if(!projectRepository.userPartOfProject(projectId, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }

        // Assuming the project entity contains a reference to the associated test entity
        Long testId = projectEntity.getTestId();

        // Update the test entity with the new files
        TestEntity testEntity = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with id: " + testId));
        try {
            // Update the test entity with the new files and upload them to the server.
            testEntity.setDockerImage(saveTest(dockerImage, projectId, userId));
            testEntity.setDockerTest(saveTest(dockerTest, projectId, userId));
            testEntity.setStructureTestId(saveTest(structureTest, projectId, userId));

            // Save the updated test entity
            testRepository.save(testEntity);

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving file: " + e.getMessage());
        }

        return ResponseEntity.ok("Tests updated successfully.");
    }


    // Hulpfunctie om de testen over te zetten naar de server en de database op de correcte plaats
    private long saveTest(MultipartFile file, long projectId, long userId) throws IOException {
        // Check if the file is empty
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Create directory if it doesn't exist
        Path projectDirectory = Paths.get("/data/projects/" + projectId + "/tests/");
        if (!Files.exists(projectDirectory)) {
            Files.createDirectories(projectDirectory);
        }

        // Save the file to the server
        Path filePath = projectDirectory.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.write(filePath, file.getBytes());

        // Save the file entity to the database and return its ID
        FileEntity fileEntity = new FileEntity(file.getOriginalFilename(), filePath.toString(), userId);
        FileEntity savedFileEntity = fileRepository.save(fileEntity);
        return savedFileEntity.getId();
    }
}

