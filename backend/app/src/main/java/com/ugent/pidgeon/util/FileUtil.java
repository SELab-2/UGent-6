package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

@Component
public class FileUtil {

    @Autowired
    private FileRepository fileRepository;

    // Hulpfunction to save the file entity to the database
    public FileEntity saveFileEntity(Path filePath, long projectId, long userId) throws IOException {
        // Save the file entity to the database
        Logger.getGlobal().info("file path: " + filePath.toString());
        Logger.getGlobal().info("file name: " + filePath.getFileName().toString());
        FileEntity fileEntity = new FileEntity(filePath.getFileName().toString(), filePath.toString(), userId);
        return fileRepository.save(fileEntity);
    }

    public CheckResult<Void> deleteFileById(long fileId) {
        FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
        if (fileEntity == null) {
            return new CheckResult<>(HttpStatus.NOT_FOUND, "File not found", null);
        }
        try {
            Filehandler.deleteLocation(Path.of(fileEntity.getPath()));
        } catch (IOException e) {
            return new CheckResult<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting file", null);
        }
        fileRepository.delete(fileEntity);
        return new CheckResult<>(HttpStatus.OK, "", null);
    }
}
