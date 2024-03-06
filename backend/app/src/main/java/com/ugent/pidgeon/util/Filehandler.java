package com.ugent.pidgeon.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
                Logger.getLogger("Filehandler").info("Creating directory: " + uploadDirectory);
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
            throw new IOException("Error while saving file" + e.getMessage());
        }
    }

    static public Path getSubmissionPath(long projectid, long groupid, long submissionid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), String.valueOf(groupid), String.valueOf(submissionid));
    }

    static boolean isZipFile(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

            byte[] signature = new byte[4];
            bufferedInputStream.mark(4);
            int bytesRead = bufferedInputStream.read(signature);

            if (bytesRead != 4) {
                throw new IOException("Error while reading file");
            }

            // Check if the file signature matches the ZIP format
            return (signature[0] == 0x50 && signature[1] == 0x4b && signature[2] == 0x03 && signature[3] == 0x04);
        }
    }

    public static ZipFile getSubmission(long submissionid) {
        Path directory = getSubmissionPath(1, 1, submissionid);
        Path filePath = directory.resolve("files.zip");
        try {
            return new ZipFile(filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
