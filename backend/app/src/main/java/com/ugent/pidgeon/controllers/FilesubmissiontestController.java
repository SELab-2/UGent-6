package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.GroupRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import com.ugent.pidgeon.util.Filehandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.zip.ZipFile;

@RestController
public class FilesubmissiontestController {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private SubmissionRepository submissionRepository;

    @PostMapping("/project/{projectid}") //Route to submit a file, it accepts a multiform with the file and submissionTime
    public ResponseEntity<String> submitFile(@RequestParam("file") MultipartFile file, @RequestParam("submissionTime") Timestamp time, @PathVariable("projectid") long projectid) {
        long userId = 1L; //TODO: replace with id of current user
        Long groupId = groupRepository.groupIdByProjectAndUser(projectid, userId);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

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

    @GetMapping("submission/{submissionid}")
    public ZipFile getSubmission(@PathVariable("submissionid") long submissionid) {
        Filehandler filehandler = new Filehandler();
        return Filehandler.getSubmission(submissionid);
    }

}
