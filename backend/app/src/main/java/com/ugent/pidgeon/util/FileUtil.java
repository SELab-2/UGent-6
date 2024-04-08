package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
