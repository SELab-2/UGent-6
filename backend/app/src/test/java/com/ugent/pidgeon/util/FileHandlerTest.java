package com.ugent.pidgeon.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

public class FileHandlerTest {


  @TempDir
  static Path tempDir;

  private MockMultipartFile file;
  private final String basicZipFileName = "Testfile.zip";
  private byte[] fileContent;
  private final Path testFilePath = Path.of("src/test/test-cases/FilehandlerTestFiles");

  @AfterEach
  public void cleanup() throws Exception {
    // Cleanup the files
    try (Stream<Path> paths = Files.walk(tempDir)) {
      paths.map(Path::toFile)
          .forEach(File::delete);
    }
  }


  @BeforeEach
  public void setUp() throws IOException {
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

}
