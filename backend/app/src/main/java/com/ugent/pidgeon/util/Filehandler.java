package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import org.apache.tika.Tika;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

public class Filehandler {

    static String BASEPATH = "data";
    static String SUBMISSION_FILENAME = "files.zip";

    public static String saveSubmission(Path directory, MultipartFile file) throws IOException {
        // Check if the file is empty
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        try {
            // Create a temporary file and save the uploaded file to it
            File tempFile = File.createTempFile("uploaded-zip-", ".zip");
            file.transferTo(tempFile);

            // Check if the file is a ZIP file
            if (!isZipFile(tempFile)) {
                throw new IOException("File is not a ZIP file");
            }
            // Create directory
            File uploadDirectory = new File(directory.toString());
            if (!uploadDirectory.exists()) {
                if(!uploadDirectory.mkdirs()) {
                    throw new IOException("Error while creating directory");
                }
            }

            // Save the file to the server
            Path filePath = directory.resolve(SUBMISSION_FILENAME);

            try(InputStream stream = new FileInputStream(tempFile)) {
                Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return filePath.getFileName().toString();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    static public Path getSubmissionPath(long projectid, long groupid, long submissionid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), String.valueOf(groupid), String.valueOf(submissionid));
    }

    public static boolean isZipFile(File file) throws IOException {
        // Create a Tika instance
        Tika tika = new Tika();

        // Detect the file type
        String fileType = tika.detect(file);

        // Check if the detected file type is a ZIP file
        Logger.getGlobal().info("File type: " + fileType);
        return fileType.equals("application/zip") || fileType.equals("application/x-zip-compressed");

    }

    public static Resource getSubmissionAsResource(Path path) throws IOException {
        return new InputStreamResource(new FileInputStream(path.toFile()));
    }

    // Hulpfunctie om de testbestanden over te zetten naar de server
    public static Path saveTest(MultipartFile file, long projectId) throws IOException {
        // Check if the file is empty
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Create directory if it doesn't exist
        Path projectDirectory = Paths.get("/data/projects/" + projectId + "/tests/");
        if (!Files.exists(projectDirectory)) {
            Files.createDirectories(projectDirectory);
        }

        // Save the file to the server
        Path filePath = projectDirectory.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.write(filePath, file.getBytes());

        return filePath;
    }
}
