package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.util.Filehandler;
import com.ugent.pidgeon.postgre.models.ProjectEntity;
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
    @Roles({UserRole.teacher})
    public ResponseEntity<String> updateTests(
            @RequestParam("dockerimage") MultipartFile dockerImage,
            @RequestParam("dockertest") MultipartFile dockerTest,
            @RequestParam("structuretest") MultipartFile structureTest,
            @PathVariable("projectid") long projectId,
            Auth auth) {

        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        long userId = auth.getUserEntity().getId();
        if(!projectRepository.userPartOfProject(projectId, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        try {
            // Save the files
            Path dockerImagePath = Filehandler.saveTest(dockerImage, projectId);
            Path dockerTestPath = Filehandler.saveTest(dockerTest, projectId);
            Path structureTestPath = Filehandler.saveTest(structureTest, projectId);

            saveFileEntity(dockerImagePath, projectId, userId);
            saveFileEntity(dockerTestPath, projectId, userId);
            saveFileEntity(structureTestPath, projectId, userId);

            return ResponseEntity.ok("Tests updated successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving files: " + e.getMessage());
        }
    }

    // Hulpfunctie om de tests correct op de database te zetten
    private void saveFileEntity(Path filePath, long projectId, long userId) throws IOException {
        // Save the file entity to the database
        FileEntity fileEntity = new FileEntity(filePath.getFileName().toString(), filePath.toString(), userId);
        fileRepository.save(fileEntity);
    }

}

