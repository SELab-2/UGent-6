package com.ugent.pidgeon.util;

import com.ugent.pidgeon.postgre.models.FileEntity;
import com.ugent.pidgeon.postgre.repository.FileRepository;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

    /**
     * Save a submission to the server
     * @param directory directory to save the submission to
     * @param file file to save
     * @return the saved file
     * @throws IOException if an error occurs while saving the file
     */
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


    /**
     * Delete a submission from the server
     * @param directory directory of the submission to delete
     * @throws IOException if an error occurs while deleting the submission
     */
    public static void deleteSubmission(Path directory) throws IOException {
        deleteLocation(directory);
    }

    /**
     * Delete a directory and all its contents
     * @param directory directory to delete
     * @throws IOException if an error occurs while deleting the directory
     */
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

    /**
     * Delete empty parent directories of a directory
     * @param directory directory to delete
     */
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


    /**
     * Get the path were a submission is stored
     * @param projectid id of the project
     * @param groupid id of the group
     * @param submissionid id of the submission
     * @return the path of the submission
     */
    static public Path getSubmissionPath(long projectid, long groupid, long submissionid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), String.valueOf(groupid), String.valueOf(submissionid));
    }

    /**
     * Get the path were a test is stored
     * @param projectid id of the project
     * @return the path of the test
     */
    static public Path getTestPath(long projectid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), "tests");
    }

    /**
     * Get a file as a resource
     * @param path path of the file
     * @return the file as a resource
     */
    public static Resource getFileAsResource(Path path) {
        File file =  path.toFile();
        return new FileSystemResource(file);
    }

    /**
     * Check if a file is a ZIP file
     * @param file file to check
     * @return true if the file is a ZIP file, false otherwise
     * @throws IOException if an error occurs while checking the file
     */
    public static boolean isZipFile(File file) throws IOException {
        // Create a Tika instance
        Tika tika = new Tika();

        // Detect the file type
        String fileType = tika.detect(file);

        // Check if the detected file type is a ZIP file
        Logger.getGlobal().info("File type: " + fileType);
        return fileType.equals("application/zip") || fileType.equals("application/x-zip-compressed");

    }

    /**
     * Get a submission as a resource
     * @param path path of the submission
     * @return the submission as a resource
     * @throws IOException if an error occurs while getting the submission
     */
    public static Resource getSubmissionAsResource(Path path) throws IOException {
        return new InputStreamResource(new FileInputStream(path.toFile()));
    }

    /**
     * Save a file to the server
     * @param file file to save
     * @param projectId id of the project
     * @return the path of the saved file
     * @throws IOException if an error occurs while saving the file
     */
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

    /**
     * Copy a file to the server project directory.
     * @param sourceFilePath the path of the file to copy
     * @param projectId the ID of the project
     * @return the path of the copied file
     * @throws IOException if an error occurs while copying the file
     */
    public static Path copyTest(Path sourceFilePath, long projectId) throws IOException {
        // Check if the source file exists
        if (!Files.exists(sourceFilePath)) {
            throw new IOException("Source file does not exist");
        }

        // Create project directory if it doesn't exist
        Path projectDirectory = getTestPath(projectId);
        if (!Files.exists(projectDirectory)) {
            Files.createDirectories(projectDirectory);
        }

        // Resolve destination file path
        Path destinationFilePath = projectDirectory.resolve(sourceFilePath.getFileName());

        // Copy the file to the project directory
        Files.copy(sourceFilePath, destinationFilePath);

        return destinationFilePath;
    }

    /**
     * Get the structure test file contents as string
     * @param path path of the structure test file
     * @return the structure test file contents as string
     * @throws IOException if an error occurs while reading the file
     */
    public static String getStructureTestString(Path path) throws IOException {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IOException("Error while reading testfile: " + e.getMessage());
        }
    }

    /**
     * A function for copying internally made lists of files, to a required path.
     * @param files list of files to copy
     * @param path path to copy the files to
     * @throws IOException if an error occurs while copying the files
     */
    public static void copyFilesAsZip(List<File> files, Path path) throws IOException {
        // Write directly to a zip file in the path variable
        File zipFile = new File(path.toString());
        System.out.println(zipFile.getAbsolutePath());
        Logger.getGlobal().info("Filexists: " + zipFile.exists());
        if (zipFile.exists() && !zipFile.canWrite()) {
            Logger.getGlobal().info("Setting writable");
            boolean res = zipFile.setWritable(true);
            if (!res) {
                throw new IOException("Cannot write to zip file");
            }
        }

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : files) {
                // add file to zip
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
                fileInputStream.close();
                zipOutputStream.closeEntry();
            }
        }
    }
}
