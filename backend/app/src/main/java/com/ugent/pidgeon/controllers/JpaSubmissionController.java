package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import com.ugent.pidgeon.postgre.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class JpaSubmissionController {
    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private FileRepository fileRepository;

    @GetMapping("/api/submissions")
    public List<String> getSubmissions() {
        List<String> res = new ArrayList<>();
        for (SubmissionEntity submission : submissionRepository.findAll()) {
            StringBuilder submissionString = new StringBuilder();
            submissionString.append(submission.getSubmissionTime()).append(" with files: ");
            Optional<FileEntity> file = fileRepository.findById(submission.getFileId());
            file.ifPresent(fileEntity -> submissionString.append(fileEntity.getName()).append(", "));

            submissionString.append("|");
            res.add(submissionString.toString());
        }
        return res;
    }

}
