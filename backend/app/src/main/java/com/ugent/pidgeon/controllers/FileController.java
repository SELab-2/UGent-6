package com.ugent.pidgeon.controllers;

import com.ugent.pidgeon.auth.Roles;
import com.ugent.pidgeon.model.Auth;
import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.models.SubmissionEntity;
import com.ugent.pidgeon.postgre.models.types.UserRole;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController {

    @Autowired
    private FileRepository fileRepository;


    public ResponseEntity<?> deleteFileById( long fileId, Auth auth) {
        // Get the submission entry from the database
        FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
        if (fileEntity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        fileRepository.delete(fileEntity);
        return  ResponseEntity.ok(fileEntity);
    }
}
