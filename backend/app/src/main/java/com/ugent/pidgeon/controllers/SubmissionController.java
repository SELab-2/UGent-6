package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.model.json.LastGroupSubmissionJson;
import com.ugent.pidgeon.model.json.SubmissionJson;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTemplateModel;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.TestEntity;
import com.ugent.pidgeon.postgre.models.UserEntity;
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
import java.util.List;
import java.util.logging.Logger;
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

    private Boolean runStructureTest(ZipFile file, TestEntity testEntity) throws IOException {
        // Get the test file from the server
        FileEntity testfileEntity = fileRepository.findById(testEntity.getStructureTestId()).orElse(null);
        if (testfileEntity == null) {
            return null;
        }
        String testfile = Filehandler.getStructureTestString(Path.of(testfileEntity.getPath()));

        // Parse the file
        SubmissionTemplateModel model = new SubmissionTemplateModel();
        model.parseSubmissionTemplate(testfile);

        return model.checkSubmission(file);
    }

    private SubmissionJson getSubmissionJson(SubmissionEntity submission) {
        return new SubmissionJson(
                submission.getId(),
                ApiRoutes.PROJECT_BASE_PATH + "/" + submission.getProjectId(),
                ApiRoutes.GROUP_BASE_PATH + "/" + submission.getGroupId(),
                ApiRoutes.SUBMISSION_BASE_PATH + "/" + submission.getId() + "/file",
                submission.getAccepted(),
                submission.getSubmissionTime());
    }

    public boolean accesToSubmission(SubmissionEntity submission, UserEntity user) {
        boolean inGroup = groupRepository.userInGroup(submission.getGroupId(), user.getId());
        boolean isAdmin = (user.getRole() == UserRole.admin) || (projectRepository.adminOfProject(submission.getProjectId(), user.getId()));
        return inGroup || isAdmin;
    }

    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}") //Route to get a submission
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<SubmissionJson> getSubmission(@PathVariable("submissionid") long submissionid, Auth auth) {
        long userId = auth.getUserEntity().getId();
        // Get the submission entry from the database
        SubmissionEntity submission = submissionRepository.findById(submissionid).orElse(null);
        if (submission == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!accesToSubmission(submission, auth.getUserEntity())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        SubmissionJson submissionJson = getSubmissionJson(submission);

        return ResponseEntity.ok(submissionJson);
    }

    @GetMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/submissions") //Route to get all submissions for a project
    @Roles({UserRole.teacher})
    public ResponseEntity<?> getSubmissions(@PathVariable("projectid") long projectid, Auth auth) {
        long userId = auth.getUserEntity().getId();
        if (!projectRepository.userPartOfProject(projectid, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You aren't part of this project");
        }

        List<Long> projectGroupIds = projectRepository.findGroupIdsByProjectId(projectid);
        List<LastGroupSubmissionJson> res = projectGroupIds.stream().map(groupId -> {
            Long submissionId = submissionRepository.findLatestsSubmissionIdsByProjectAndGroupId(projectid, groupId);
            if (submissionId == null) {
                return new LastGroupSubmissionJson(
                    ApiRoutes.GROUP_BASE_PATH + "/" + groupId, null
                );
            }
            return new LastGroupSubmissionJson(
                    ApiRoutes.GROUP_BASE_PATH + "/" + groupId,
                    ApiRoutes.SUBMISSION_BASE_PATH + "/" + submissionId
            );
        }).toList();
        return ResponseEntity.ok(res);
    }


    @PostMapping(ApiRoutes.PROJECT_BASE_PATH + "/{projectid}/submit") //Route to submit a file, it accepts a multiform with the file and submissionTime
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> submitFile(@RequestParam("file") MultipartFile file, @RequestParam("submissionTime") Timestamp time, @PathVariable("projectid") long projectid,Auth auth) {
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

            //Save the submission in the database
            SubmissionEntity submission = submissionRepository.save(submissionEntity);

            //Save the file on the server
            String filename = file.getOriginalFilename();
            Path path = Filehandler.getSubmissionPath(projectid, groupId, submission.getId());
            File savedFile = Filehandler.saveSubmission(path, file);
            String pathname = path + "/" + Filehandler.SUBMISSION_FILENAME;

            //Update name and path for the file entry
            fileEntity.setName(filename);
            fileEntity.setPath(pathname);
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

            return ResponseEntity.ok(getSubmissionJson(submissionEntity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving file: " + e.getMessage());
        }

    }

    @GetMapping(ApiRoutes.SUBMISSION_BASE_PATH + "/{submissionid}/file") //Route to get a submission
    @Roles({UserRole.teacher, UserRole.student})
    public ResponseEntity<?> getSubmissionFile(@PathVariable("submissionid") long submissionid, Auth auth) {
        long userId = auth.getUserEntity().getId();
        // Get the submission entry from the database
        SubmissionEntity submission = submissionRepository.findById(submissionid).orElse(null);
        if (submission == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!accesToSubmission(submission, auth.getUserEntity())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        // Get the file entry from the database
        FileEntity file = fileRepository.findById(submission.getFileId()).orElse(null);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }


        // Get the file from the server
        try {
            Resource zipFile = Filehandler.getSubmissionAsResource(Path.of(file.getPath()));

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipFile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
