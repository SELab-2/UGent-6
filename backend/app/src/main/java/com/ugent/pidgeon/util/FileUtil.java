package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import java.io.File;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FileUtil {

    @Autowired
    private FileRepository fileRepository;

    /**
     * Delete a file by id from the database and server
     * @param fileId id of the file
     * @return CheckResult with the status of the check
     */
    public CheckResult<Void> deleteFileById(long fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
        if (fileEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "File not found", null);
        }
        try {
            Path path = Path.of(fileEntity.getPath());
            Filehandler.deleteLocation(new File(path.toString()));
        } catch (Exception e) {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting file", null);
        }
        fileRepository.delete(fileEntity);
        return new CheckResult<>(HttpStatus.OK, "", null);
    }
}
