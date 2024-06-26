package com.ugent.pidgeon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.tika.Tika;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class Filehandler {

    static String BASEPATH = "data";
    public static String SUBMISSION_FILENAME = "files.zip";
    public static String EXTRA_TESTFILES_FILENAME = "testfiles.zip";
    public static String ADMIN_SUBMISSION_FOLDER = "adminsubmissions";

    /**
     * Save a submission to the server
     * @param directory directory to save the submission to
     * @param file file to save
     * @return the saved file
     * @throws IOException if an error occurs while saving the file
     */
    public static File saveFile(Path directory, MultipartFile file, String filename) throws IOException {
        // Check if the file is empty
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        try {
            // Create a temporary file and save the uploaded file to it
            File tempFile = File.createTempFile("SELAB6CANDELETEuploaded-zip-", ".zip");
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
            Path filePath = directory.resolve(filename);

            try(InputStream stream = new FileInputStream(tempFile)) {
                Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return filePath.toFile();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }



    /**
     * Delete a directory and all its contents, eg: deleteLocation(new File(path.toString())
     * @param uploadDirectory File representing directory to delete
     * @throws IOException if an error occurs while deleting the directory
     */
    public static void deleteLocation(File uploadDirectory) throws IOException {
        try {
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
    private static void deleteEmptyParentDirectories(File directory) throws IOException {
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length == 0) {
                if (!directory.delete()) {
                    throw new IOException("Error while deleting empty directory: " + directory.getAbsolutePath());
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
    static public Path getSubmissionPath(long projectid, Long groupid, long submissionid) {
        if (groupid == null) {
            return Path.of(BASEPATH,"projects", String.valueOf(projectid), ADMIN_SUBMISSION_FOLDER, String.valueOf(submissionid));
        }
        return Path.of(BASEPATH,"projects", String.valueOf(projectid), String.valueOf(groupid), String.valueOf(submissionid));
    }

    static public Path getSubmissionArtifactPath(long projectid, Long groupid, long submissionid) {
        return getSubmissionPath(projectid, groupid, submissionid).resolve("artifacts.zip");
    }

    static public Path getTestExtraFilesPath(long projectid) {
        return Path.of(BASEPATH,"projects", String.valueOf(projectid));
    }

    /**
     * Get a file as a resource
     * @param path path of the file
     * @return the file as a resource
     */
    public static Resource getFileAsResource(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
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

    public static ResponseEntity<?> getZipFileAsResponse(Path path, String filename) {
        // Get the file from the server
        Resource zipFile = Filehandler.getFileAsResource(path);
        if (zipFile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found.");
        }

        // Set headers for the response
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

        return ResponseEntity.ok()
            .headers(headers)
            .body(zipFile);
    }


    public static void addExistingZip(ZipOutputStream groupZipOut, String zipFileName, File zipFile) throws IOException {
        ZipEntry zipEntry = new ZipEntry(zipFileName);
        groupZipOut.putNextEntry(zipEntry);

        // Read the content of the zip file and write it to the group zip output stream
        Files.copy(zipFile.toPath(), groupZipOut);

        groupZipOut.closeEntry();
    }
}
