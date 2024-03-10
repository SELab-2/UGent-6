package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.*;
import com.ugent.pidgeon.util.Filehandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.zip.ZipFile;

@RestController
public class SubmissionController {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TestRepository testRepository;

    public Boolean runStructureTest(ZipFile file, TestEntity testEntity) throws IOException {
        // Get the test file from the server
        FileEntity testfileEntity = fileRepository.findById(testEntity.getStructureTestId()).orElse(null);
        if (testfileEntity == null) {
            return null;
        }
        String testfile = Filehandler.getStructureTestString(Path.of(testfileEntity.getPath(), testfileEntity.getName()));

        // Parse the file
        SubmissionTemplateModel model = new SubmissionTemplateModel();
        model.parseSubmissionTemplate(testfile);

        return model.checkSubmission(file);
    }

    @PostMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/submit") //Route to submit a file, it accepts a multiform with the file and submissionTime
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> submitFile(@RequestParam("file") MultipartFile file, @RequestParam("submissionTime") Timestamp time, @PathVariable("projectid") long projectid,Auth auth) {
        long userId = auth.getUserEntity().getId();
        Long groupId = groupRepository.groupIdByProjectAndUser(projectid, userId);

        if (!projectRepository.userPartOfProject(projectid, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }

        //TODO: execute the docker tests onces these are implemented
        try {
            //Save the file entry in the database to get the id
            FileEntity fileEntity = new FileEntity("", "", userId);
            long fileid = fileRepository.save(fileEntity).getId();


            SubmissionEntity submissionEntity = new SubmissionEntity(projectid, groupId, fileid, time, false);

            //Save the submission in the database TODO: update the accepted parameter
            SubmissionEntity submission = submissionRepository.save(submissionEntity);

            //Save the file on the server
            Path path = Filehandler.getSubmissionPath(projectid, groupId, submission.getId());
            File savedFile = Filehandler.saveSubmission(path, file);
            String filename = savedFile.getName();

            //Update name and path for the file entry
            fileEntity.setName(filename);
            fileEntity.setPath(path.toString());
            fileRepository.save(fileEntity);

            // Run structure tests
            TestEntity testEntity = testRepository.findByProjectId(projectid).orElse(null);
            if (testEntity == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No tests found for this project");
            }
            Boolean testresult = runStructureTest(new ZipFile(savedFile), testEntity);
            if (testresult == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while running tests: test files not found");
            }

            submissionEntity.setAccepted(testresult);
            submissionRepository.save(submissionEntity);

            return ResponseEntity.ok("File saved");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving file: " + e.getMessage());
        }

    }

    @GetMapping("submissions/{submissionid}")
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<Resource> getSubmission(@PathVariable("submissionid") long submissionid, Auth auth) {
        long userId = auth.getUserEntity().getId();
        // Get the submission entry from the database
        SubmissionEntity submission = submissionRepository.findById(submissionid).orElse(null);
        if (submission == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!groupRepository.userInGroup(submission.getGroupId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        // Get the file entry from the database
        FileEntity file = fileRepository.findById(submission.getFileId()).orElse(null);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }


        // Get the file from the server
        try {
            Resource zipFile = Filehandler.getSubmissionAsResource(Path.of(file.getPath(), file.getName()));

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
