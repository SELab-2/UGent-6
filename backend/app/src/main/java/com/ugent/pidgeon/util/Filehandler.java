package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import org.apache.tika.Tika;
import org.springframework.core.io.FileSystemResource;
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
    public static String SUBMISSION_FILENAME = "files.zip";

    public static File saveSubmission(Path directory, MultipartFile file) throws IOException {
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

            return tempFile;
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }



    public static void deleteSubmission(Path directory) throws IOException {
        deleteLocation(directory);
    }

    public static void deleteLocation(Path directory) throws IOException {
        try {
            File uploadDirectory = new File(directory.toString());
            if (uploadDirectory.exists()) {
                if(!uploadDirectory.delete()) {
                    throw new IOException("Error while deleting directory");
                }
                deleteEmptyParentDirectories(uploadDirectory.getParentFile());
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static void deleteEmptyParentDirectories(File directory) {
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length == 0) {
                if (!directory.delete()) {
                    System.err.println("Error while deleting empty directory: " + directory.getAbsolutePath());
                } else {
                    deleteEmptyParentDirectories(directory.getParentFile());
                }
            }
        }
    }


    static public Path getSubmissionPath(long projectid, long groupid, long submissionid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), String.valueOf(groupid), String.valueOf(submissionid));
    }

    static public Path getTestPath(long projectid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), "tests");
    }

    static public void deleteTest(long projectid) throws IOException {
        try {
            File uploadDirectory = new File(getTestPath(projectid).toString());
            if (uploadDirectory.exists()) {
                if(!uploadDirectory.delete()) {
                    throw new IOException("Error while deleting directory");
                }
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public static File getFile(Path path) {
        return path.toFile();
    }

    public static Resource getFileAsResource(Path path) {
        File file =  path.toFile();
        return new FileSystemResource(file);
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

    // helper function to save a file to the server
    public static Path saveTest(MultipartFile file, long projectId) throws IOException {
        // Check if the file is empty
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Create directory if it doesn't exist
        Path projectDirectory = getTestPath(projectId);
        if (!Files.exists(projectDirectory)) {
            Files.createDirectories(projectDirectory);
        }

        // Save the file to the server
        Path filePath = projectDirectory.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.write(filePath, file.getBytes());

        return filePath;
    }

    public static String getStructureTestString(Path path) throws IOException {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IOException("Error while reading testfile: " + e.getMessage());
        }
    }
}
