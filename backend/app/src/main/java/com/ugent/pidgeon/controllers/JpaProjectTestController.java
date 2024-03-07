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

import java.io.IOException;
import java.util.List;

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
            @RequestParam("dockerfile") MultipartFile dockerfile,
            @RequestParam("structurefile") MultipartFile structurefile,
            @PathVariable("projectid") long projectId) {

//        // Delete existing test files linked to the current project
//        deleteExistingTestFiles(projectId);
//
//        // Save new test files
//        FileEntity dockerFileEntity = saveFile(dockerfile);
//        FileEntity structureFileEntity = saveFile(structurefile);
//
//        // Create a new test entity
//        TestEntity testEntity = new TestEntity();
//        testEntity.setDockerFile(dockerFileEntity);
//        testEntity.setStructureFile(structureFileEntity);
//        testRepository.save(testEntity);
//
//        // Update project-test relationship
//        ProjectEntity projectEntity = projectRepository.findById(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
//        projectEntity.setTests(List.of(testEntity));
//        projectRepository.save(projectEntity);

        return ResponseEntity.ok("Tests updated successfully.");
    }

//    private void deleteExistingTestFiles(long projectId) {
//        // Retrieve the project entity
//        ProjectEntity projectEntity = projectRepository.findById(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));
//
//        // Delete existing test files linked to the project
//        testRepository.deleteAll(existingTests);
//    }
//
//    private FileEntity saveFile(MultipartFile multipartFile) throws IOException {
//        // Save the file to the database and return the corresponding file entity
//        // Implement this method according to your file storage mechanism
//        // For simplicity, let's assume the file is saved directly to the database
//        FileEntity fileEntity = new FileEntity();
//        fileEntity.setFileName(multipartFile.getOriginalFilename());
//        fileEntity.setFileData(multipartFile.getBytes());
//        return fileRepository.save(fileEntity);
//    }
}

