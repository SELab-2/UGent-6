package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.ProjectRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import com.ugent.pidgeon.util.Filehandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.sql.Timestamp;

@RestController
public class FilesubmissiontestController {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileController fileController;


    @PostMapping("/project/{projectid}/submit") //Route to submit a file, it accepts a multiform with the file and submissionTime
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<String> submitFile(@RequestParam("file") MultipartFile file, @RequestParam("submissionTime") Timestamp time, @PathVariable("projectid") long projectid,Auth auth) {
        long userId = auth.getUserEntity().getId();
        Long groupId = groupRepository.groupIdByProjectAndUser(projectid, userId);

        if (!projectRepository.userPartOfProject(projectid, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }
        //TODO: executes the tests onces these are implemented
        try {
            //Save the file entry in the database to get the id
            FileEntity fileEntity = new FileEntity("", "", userId);
            long fileid = fileRepository.save(fileEntity).getId();

            //Save the submission in the database TODO: update the accepted parameter
            SubmissionEntity submissionEntity = new SubmissionEntity(projectid, groupId, fileid, time, false);
            SubmissionEntity submission = submissionRepository.save(submissionEntity);

            //Save the file on the server
            Path path = Filehandler.getSubmissionPath(projectid, groupId, submission.getId());
            String filename = Filehandler.saveSubmission(path, file);

            //Update name and path for the file entry
            fileEntity.setName(filename);
            fileEntity.setPath(path.toString());
            fileRepository.save(fileEntity);

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


    @DeleteMapping(ApiRoutes.SUBMISSION_BASE_PATH+"/{submissionid}")
    @Roles({UserRole.admin, UserRole.teacher})
    public ResponseEntity<?> deleteSubmissionById(@PathVariable("submissionid") long submissionid, Auth auth) {
        long userId = auth.getUserEntity().getId();
        // Get the submission entry from the database
        SubmissionEntity submission = submissionRepository.findById(submissionid).orElse(null);
        if (submission == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!groupRepository.userInGroup(submission.getGroupId(), userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        fileController.deleteFileById(submission.getFileId(),auth);
        submissionRepository.delete(submission);
        return  ResponseEntity.ok(submission);
    }
}
