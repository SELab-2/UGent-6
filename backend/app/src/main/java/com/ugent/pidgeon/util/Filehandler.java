package com.ugent.pidgeon.util;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.tika.Tika;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class Filehandler {

    static String BASEPATH = "data";
    public static String SUBMISSION_FILENAME = "files.zip";
    public static String ADMIN_SUBMISSION_FOLDER = "adminsubmissions";

    /**
     * Save a submission to the server
     * @param directory directory to save the submission to
     * @param file file to save
     * @return the saved file
     * @throws IOException if an error occurs while saving the file
     */
    public static File saveSubmission(Path directory, MultipartFile file) throws IOException {
        // Check if the file is empty
        if (file == null || file.isEmpty()) {
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
}
