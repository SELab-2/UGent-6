package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.util.Filehandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.zip.ZipFile;

@RestController
public class FilesubmissiontestController {

    @PostMapping("/project/{projectid}")
    public ResponseEntity<String> submitFile(@RequestParam("file") MultipartFile file, @PathVariable("projectid") long projectid) {
        Filehandler filehandler = new Filehandler();
        try {
            filehandler.saveSubmission(projectid, 1, 1, file);
            return ResponseEntity.ok("File saved");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving file: " + e.getMessage());
        }

    }

    @GetMapping("submission/{submissionid}")
    public ZipFile getSubmission(@PathVariable("submissionid") long submissionid) {
        Filehandler filehandler = new Filehandler();
        return filehandler.getSubmission(submissionid);
    }

}
