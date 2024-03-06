package com.ugent.pidgeon.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

public class Filehandler {

    public void saveSubmission(long projectid, long groupid, long submissionid, MultipartFile file) throws IOException {
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
            Path directory = getPath(projectid, groupid, submissionid);
            File uploadDirectory = new File(directory.toString());
            if (!uploadDirectory.exists()) {
                if(!uploadDirectory.mkdir()) {
                    throw new IOException("Error while creating directory");
                }
            }

            // Save the file to the server
            Path filePath = directory.resolve("files.zip");
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(filePath.toString()));
            stream.write(file.getBytes());
            stream.close();

        } catch (IOException e) {
            throw new IOException("Error while saving file" + e.getMessage());
        }
    }

    public Path getPath(long projectid, long groupid, long submissionid) {
        return Path.of("projects", String.valueOf(projectid), String.valueOf(groupid), String.valueOf(submissionid));
    }

    private boolean isZipFile(File file) throws IOException {
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
}
