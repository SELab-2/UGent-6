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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
      tempDir = Files.createTempDirectory("test");
      fileContent = Files.readAllBytes(testFilePath.resolve(basicZipFileName));
      file = new MockMultipartFile(
          basicZipFileName, fileContent
      );
  }

  @Test
  public void testSaveSubmission() throws Exception {
      File savedFile = Filehandler.saveSubmission(tempDir, file);

      assertTrue(savedFile.exists());
      assertEquals(Filehandler.SUBMISSION_FILENAME, savedFile.getName());
      assertEquals(fileContent.length, savedFile.length());
      byte[] savedFileContent = Files.readAllBytes(savedFile.toPath());
      assertEquals(fileContent.length, savedFileContent.length);
  }

  @Test
  public void testSaveSubmission_dirDoesntExist() throws Exception {
    File savedFile = Filehandler.saveSubmission(tempDir.resolve("nonexistent"), file);

    assertTrue(savedFile.exists());
    assertEquals(Filehandler.SUBMISSION_FILENAME, savedFile.getName());
    assertEquals(fileContent.length, savedFile.length());
    byte[] savedFileContent = Files.readAllBytes(savedFile.toPath());
    assertEquals(fileContent.length, savedFileContent.length);
  }

  @Test
  public void testSaveSubmission_errorWhileCreatingDir() throws Exception {
    assertThrows(IOException.class, () -> Filehandler.saveSubmission(Path.of(""), file));
  }

  @Test
  public void testSaveSubmission_notAZipFile() {
    MockMultipartFile notAZipFile = new MockMultipartFile(
        "notAZipFile.txt", "This is not a zip file".getBytes()
    );
    assertThrows(IOException.class, () -> Filehandler.saveSubmission(tempDir, notAZipFile));
  }

  @Test
  public void testSaveSubmission_fileEmpty() {
    MockMultipartFile emptyFile = new MockMultipartFile(
        "emptyFile.txt", new byte[0]
    );
    assertThrows(IOException.class, () -> Filehandler.saveSubmission(tempDir, emptyFile));
  }

  @Test
  public void testSaveSubmission_fileNull() {
    assertThrows(IOException.class, () -> Filehandler.saveSubmission(tempDir, null));
  }

  @Test
  public void testDeleteLocation() throws Exception {
    Path testDir = Files.createTempDirectory("test");
    Path tempFile = Files.createTempFile(testDir, "test", ".txt");
    Filehandler.deleteLocation(new File(tempFile.toString()));
    assertFalse(Files.exists(testDir));
  }

  @Test
  public void testDeleteLocation_parentDirNotEmpty() throws Exception {
    Path testDir = Files.createTempDirectory("test");
    Path tempFile = Files.createTempFile(testDir, "test", ".txt");
    Files.createTempFile(testDir, "test2", ".txt");
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
    Path submissionPath = Filehandler.getSubmissionPath(1, 2, 3);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", "1", "2", "3"), submissionPath);
  }

  @Test
  public void testGetSubmissionArtifactPath() {
    Path submissionArtifactPath = Filehandler.getSubmissionArtifactPath(1, 2, 3);
    assertEquals(Path.of(Filehandler.BASEPATH, "projects", "1", "2", "3", "artifacts.zip"), submissionArtifactPath);
  }

  @Test
  public void testGetFileAsResource_FileExists() {
    try {
      File tempFile = Files.createTempFile("testFile", ".txt").toFile();

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
    File tempFile1 = Files.createTempFile("tempFile1", ".txt").toFile();
    File tempFile2 = Files.createTempFile("tempFile2", ".txt").toFile();

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
    File tempFile1 = Files.createTempFile("tempFile1", ".txt").toFile();
    File tempFile2 = Files.createTempFile("tempFile2", ".txt").toFile();
    File zipFile = Files.createTempFile(tempDir, "files", ".zip").toFile();

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
    File tempFile1 = createTempFileWithContent("tempFile1", ".txt", 4095);
    File tempFile2 = Files.createTempFile("tempFile2", ".txt").toFile();
    File zipFile = Files.createTempFile(tempDir, "files", ".zip").toFile();
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

}