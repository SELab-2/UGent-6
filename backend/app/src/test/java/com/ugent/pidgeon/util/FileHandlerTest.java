package com.ugent.pidgeon.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class FileHandlerTest {


  static Path tempDir;

  private MockMultipartFile file;
  private final String basicZipFileName = "Testfile.zip";
  private byte[] fileContent;
  private final Path testFilePath = Path.of("src/test/test-cases/FilehandlerTestFiles");

  @AfterEach
  public void cleanup() throws Exception {
    // Cleanup the files
    if (Files.exists(tempDir)) {
      try (Stream<Path> paths = Files.walk(tempDir)) {
        paths.map(Path::toFile)
            .forEach(File::delete);
      }
    }

  }


  @BeforeEach
  public void setUp() throws IOException {
      tempDir = Files.createTempDirectory("SELAB6CANDELETEtest");
      fileContent = Files.readAllBytes(testFilePath.resolve(basicZipFileName));
      file = new MockMultipartFile(
          basicZipFileName, fileContent
      );
  }

  @Test
  public void testSaveFile() throws Exception {
      File savedFile = Filehandler.saveFile(tempDir, file, Filehandler.SUBMISSION_FILENAME);

      assertTrue(savedFile.exists());
      assertEquals(Filehandler.SUBMISSION_FILENAME, savedFile.getName());
      assertEquals(fileContent.length, savedFile.length());
      byte[] savedFileContent = Files.readAllBytes(savedFile.toPath());
      assertEquals(fileContent.length, savedFileContent.length);
  }

  @Test
  public void testSaveFile_dirDoesntExist() throws Exception {
    File savedFile = Filehandler.saveFile(tempDir.resolve("nonexistent"), file, Filehandler.SUBMISSION_FILENAME);

    assertTrue(savedFile.exists());
    assertEquals(Filehandler.SUBMISSION_FILENAME, savedFile.getName());
    assertEquals(fileContent.length, savedFile.length());
    byte[] savedFileContent = Files.readAllBytes(savedFile.toPath());
    assertEquals(fileContent.length, savedFileContent.length);
  }

  @Test
  public void testSaveFile_errorWhileCreatingDir() throws Exception {
    assertThrows(IOException.class, () -> Filehandler.saveFile(Path.of(""), file, Filehandler.SUBMISSION_FILENAME));
  }

  @Test
  public void testSaveFile_notAZipFile() {
    MockMultipartFile notAZipFile = new MockMultipartFile(
        "notAZipFile.txt", "This is not a zip file".getBytes()
    );
    assertThrows(IOException.class, () -> Filehandler.saveFile(tempDir, notAZipFile, Filehandler.SUBMISSION_FILENAME));
  }

  @Test
  public void testSaveFile_fileEmpty() {
    MockMultipartFile emptyFile = new MockMultipartFile(
        "emptyFile.txt", new byte[0]
    );
    assertThrows(IOException.class, () -> Filehandler.saveFile(tempDir, emptyFile, Filehandler.SUBMISSION_FILENAME));
  }

  @Test
  public void testSaveFile_fileNull() {
    assertThrows(IOException.class, () -> Filehandler.saveFile(tempDir, null, Filehandler.SUBMISSION_FILENAME));
  }

  @Test
  public void testDeleteLocation() throws Exception {
    Path testDir = Files.createTempDirectory("SELAB6CANDELETEtest");
    Path tempFile = Files.createTempFile(testDir, "SELAB6CANDELETEtest", ".txt");
    Filehandler.deleteLocation(new File(tempFile.toString()));
    assertFalse(Files.exists(testDir));
  }

  @Test
  public void testDeleteLocation_parentDirNotEmpty() throws Exception {
    Path testDir = Files.createTempDirectory("SELAB6CANDELETEtest");
    Path tempFile = Files.createTempFile(testDir, "SELAB6CANDELETEtest", ".txt");
    Files.createTempFile(testDir, "SELAB6CANDELETEtest2", ".txt");
    Filehandler.deleteLocation(new File(tempFile.toString()));
    assertTrue(Files.exists(testDir));
  }

  @Test
  public void testDeleteLocation_locationDoesntExist() throws Exception {
    Filehandler.deleteLocation(new File("nonexistent"));
  }

  @Test
  public void testDeleteLocation_errorWhileDeleting() {
    // Create a mock File object
    File mockDir = mock(File.class);

    when(mockDir.exists()).thenReturn(true);
    when(mockDir.delete()).thenReturn(false);

    assertThrows(IOException.class, () -> Filehandler.deleteLocation(mockDir));

    verify(mockDir).exists();
    verify(mockDir).delete();
  }

  @Test
  public void testDeleteLocation_errorWhileDeletingParentDir() {
    File mockDir = mock(File.class);
    File mockParentDir = mock(File.class);
    File[] mockedFiles = new File[0];

    when(mockDir.exists()).thenReturn(true);
    when(mockDir.delete()).thenReturn(true);
    when(mockDir.getParentFile()).thenReturn(mockParentDir);
    when(mockParentDir.isDirectory()).thenReturn(true);
    when(mockParentDir.listFiles()).thenReturn(mockedFiles);
    when(mockParentDir.delete()).thenReturn(false);

    assertThrows(IOException.class, () -> Filehandler.deleteLocation(mockDir));

    verify(mockDir).exists();
    verify(mockDir).delete();
    verify(mockDir).getParentFile();
    verify(mockParentDir).listFiles();
    verify(mockParentDir).delete();
  }

  @Test
  public void testDeleteLocation_filesAreNotEmpty() throws IOException {
    File mockDir = mock(File.class);
    File mockParentDir = mock(File.class);
    File[] mockedFiles = new File[1];
    mockedFiles[0] = mock(File.class);

    when(mockDir.exists()).thenReturn(true);
    when(mockDir.delete()).thenReturn(true);
    when(mockDir.getParentFile()).thenReturn(mockParentDir);
    when(mockParentDir.isDirectory()).thenReturn(true);
    when(mockParentDir.listFiles()).thenReturn(mockedFiles);

    Filehandler.deleteLocation(mockDir);

    verify(mockDir).exists();
    verify(mockDir).delete();
    verify(mockDir).getParentFile();
    verify(mockParentDir).listFiles();
  }

  @Test
  public void testDeleteLocation_filesAreNull() throws IOException {
    File mockDir = mock(File.class);
    File mockParentDir = mock(File.class);

    when(mockDir.exists()).thenReturn(true);
    when(mockDir.delete()).thenReturn(true);
    when(mockDir.getParentFile()).thenReturn(mockParentDir);
    when(mockParentDir.isDirectory()).thenReturn(true);
    when(mockParentDir.listFiles()).thenReturn(null);

    Filehandler.deleteLocation(mockDir);

    verify(mockDir).exists();
    verify(mockDir).delete();
    verify(mockDir).getParentFile();
    verify(mockParentDir).listFiles();
  }

  @Test
  public void testDeleteLocation_parentDirIsNotADir() throws IOException {
    File mockDir = mock(File.class);
    File mockParentDir = mock(File.class);

    when(mockDir.exists()).thenReturn(true);
    when(mockDir.delete()).thenReturn(true);
    when(mockDir.getParentFile()).thenReturn(mockParentDir);
    when(mockParentDir.isDirectory()).thenReturn(false);

    Filehandler.deleteLocation(mockDir);

    verify(mockDir).exists();
    verify(mockDir).delete();
    verify(mockDir).getParentFile();
    verify(mockParentDir).isDirectory();
  }

  @Test
  public void testDeleteLocation_parentDirIsNull() throws IOException {
    File mockDir = mock(File.class);

    when(mockDir.exists()).thenReturn(true);
    when(mockDir.delete()).thenReturn(true);
    when(mockDir.getParentFile()).thenReturn(null);

    Filehandler.deleteLocation(mockDir);

    verify(mockDir).exists();
    verify(mockDir).delete();
    verify(mockDir).getParentFile();
  }

  @Test
  public void testGetSubmissionPath() {
    Path submissionPath = Filehandler.getSubmissionPath(1, 2L, 3);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", "1", "2", "3"), submissionPath);
  }

  @Test
  public void testGetSubmissionPath_groupIdIsNull() {
    Path submissionPath = Filehandler.getSubmissionPath(1, null, 3);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", "1", Filehandler.ADMIN_SUBMISSION_FOLDER, "3"), submissionPath);
  }

  @Test
  public void testGetSubmissionArtifactPath() {
    Path submissionArtifactPath = Filehandler.getSubmissionArtifactPath(1, 2L, 3);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", "1", "2", "3", "artifacts.zip"), submissionArtifactPath);
  }

  @Test

  public void testGetTextExtraFilesPath() {
    Path textExtraFilesPath = Filehandler.getTestExtraFilesPath(88);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", String.valueOf(88)), textExtraFilesPath);
  }
  @Test
  public void testGetSubmissionArtifactPath_groupIdIsNull() {
    Path submissionArtifactPath = Filehandler.getSubmissionArtifactPath(1, null, 3);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", "1", Filehandler.ADMIN_SUBMISSION_FOLDER, "3", "artifacts.zip"), submissionArtifactPath);

  }

  @Test
  public void testGetFileAsResource_FileExists() {
    try {
      File tempFile = Files.createTempFile("SELAB6CANDELETEtestFile", ".txt").toFile();

      Resource resource = Filehandler.getFileAsResource(tempFile.toPath());

      assertNotNull(resource);
      assertInstanceOf(FileSystemResource.class, resource);
      assertEquals(tempFile.getAbsolutePath(), ((FileSystemResource) resource).getFile().getAbsolutePath());

      assertTrue(tempFile.delete());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetFileAsResource_FileDoesNotExist() {
    Resource resource = Filehandler.getFileAsResource(Path.of("nonexistent"));

    assertNull(resource);
  }

  @Test
  public void testCopyFilesAsZip() throws IOException {
    List<File> files = new ArrayList<>();
    File tempFile1 = Files.createTempFile("SELAB6CANDELETEtempFile1", ".txt").toFile();
    File tempFile2 = Files.createTempFile("SELAB6CANDELETEtempFile2", ".txt").toFile();

    try {
      files.add(tempFile1);
      files.add(tempFile2);

      File zipFile = tempDir.resolve("files.zip").toFile();
      Filehandler.copyFilesAsZip(files, zipFile.toPath());

      assertTrue(zipFile.exists());

      try (ZipFile zip = new ZipFile(zipFile)) {
        for (File file : files) {
          String entryName = file.getName();
          ZipEntry zipEntry = zip.getEntry(entryName);
          assertNotNull(zipEntry, "File " + entryName + " not found in the zip file.");
        }
      }


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testCopyFilesAsZip_zipFileAlreadyExist() throws IOException {
    List<File> files = new ArrayList<>();
    File tempFile1 = Files.createTempFile("SELAB6CANDELETEtempFile1", ".txt").toFile();
    File tempFile2 = Files.createTempFile("SELAB6CANDELETEtempFile2", ".txt").toFile();
    File zipFile = Files.createTempFile(tempDir, "SELAB6CANDELETEfiles", ".zip").toFile();

    try {
      files.add(tempFile1);
      files.add(tempFile2);

      assertTrue(zipFile.exists());

      Filehandler.copyFilesAsZip(files, zipFile.toPath());

      assertTrue(zipFile.exists());

      try (ZipFile zip = new ZipFile(zipFile)) {
        for (File file : files) {
          String entryName = file.getName();
          ZipEntry zipEntry = zip.getEntry(entryName);
          assertNotNull(zipEntry, "File " + entryName + " not found in the zip file.");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static File createTempFileWithContent(String prefix, String suffix, int fileSizeInBytes) throws IOException {
    Path tempFilePath = Files.createTempFile(prefix, suffix);

    try (FileOutputStream outputStream = new FileOutputStream(tempFilePath.toFile())) {
      // Write data to the file until it reaches the desired size
      for (int i = 0; i < fileSizeInBytes; i++) {
        outputStream.write('A'); // Write a byte to the file (in this case, the letter 'A')
      }
    }

    return tempFilePath.toFile();
  }

  @Test
  public void testCopyFilesAsZip_zipFileAlreadyExistNonWriteable() throws IOException {
    List<File> files = new ArrayList<>();
    File tempFile1 = createTempFileWithContent("SELAB6CANDELETEtempFile1", ".txt", 4095);
    File tempFile2 = Files.createTempFile("SELAB6CANDELETEtempFile2", ".txt").toFile();
    File zipFile = Files.createTempFile(tempDir, "SELAB6CANDELETEfiles", ".zip").toFile();
    zipFile.setWritable(false);

    try {
      files.add(tempFile1);
      files.add(tempFile2);

      assertTrue(zipFile.exists());

      Filehandler.copyFilesAsZip(files, zipFile.toPath());

      assertTrue(zipFile.exists());

      try (ZipFile zip = new ZipFile(zipFile)) {
        for (File file : files) {
          String entryName = file.getName();
          ZipEntry zipEntry = zip.getEntry(entryName);
          assertNotNull(zipEntry, "File " + entryName + " not found in the zip file.");
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetZipFileAsResponse() throws IOException {
    List<File> files = new ArrayList<>();
    File tempFile1 = Files.createTempFile("SELAB6CANDELETEtempFile1", ".txt").toFile();
    File tempFile2 = Files.createTempFile("SELAB6CANDELETEtempFile2", ".txt").toFile();

    try {
      files.add(tempFile1);
      files.add(tempFile2);

      File zipFile = tempDir.resolve("files.zip").toFile();
      Filehandler.copyFilesAsZip(files, zipFile.toPath());

      assertTrue(zipFile.exists());

      ResponseEntity response = Filehandler.getZipFileAsResponse(zipFile.toPath(), "customfilename.zip");

      assertNotNull(response);
      assertEquals(200, response.getStatusCodeValue());
      assertEquals("attachment; filename=customfilename.zip", response.getHeaders().get("Content-Disposition").get(0));
      assertEquals("application/zip", response.getHeaders().get("Content-Type").get(0));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetZipFileAsResponse_fileDoesNotExist() {
    ResponseEntity response = Filehandler.getZipFileAsResponse(Path.of("nonexistent"), "customfilename.zip");

    assertNotNull(response);
    assertEquals(404, response.getStatusCodeValue());
  }

  @Test
  public void testAddExistingZip() throws IOException {
    // Create zip file
    String zipFileName = "existingZipFile.zip";
    File tempZipFile = Files.createTempFile("SELAB6CANDELETEexistingZip", ".zip").toFile();

    // Populate the zip file with some content
    try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
      ZipEntry entry = new ZipEntry("testFile.txt");
      tempZipOutputStream.putNextEntry(entry);
      tempZipOutputStream.write("Test content".getBytes());
      tempZipOutputStream.closeEntry();
      Filehandler.addExistingZip(tempZipOutputStream, zipFileName, tempZipFile);
    }





    // Check if the zip file contains the entry
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempZipFile))) {
      ZipEntry entry;
      boolean found = false;
      boolean originalFound = false;
      while ((entry = zis.getNextEntry()) != null) {
        Logger.getGlobal().info("Entry: " + entry.getName());
        if (entry.getName().equals(zipFileName)) {
          found = true;
        } else if (entry.getName().equals("testFile.txt")) {
          originalFound = true;
        }
      }
      assertTrue(found);
      assertTrue(originalFound);
    }
  }




}
