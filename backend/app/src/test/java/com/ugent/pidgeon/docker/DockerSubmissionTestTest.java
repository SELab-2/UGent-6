package com.ugent.pidgeon.docker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.DockerSubtestResult;
import com.ugent.pidgeon.model.submissionTesting.DockerTemplateTestOutput;
import com.ugent.pidgeon.model.submissionTesting.DockerTestOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class DockerSubmissionTestTest {

  @AfterEach
  void cleanUp() {
    File file = new File(System.getProperty("user.dir") + "/tmp/test");
    try {
      FileUtils.deleteDirectory(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  File initTestFile(String text, String fileName) {
    String localFileLocation = System.getProperty("user.dir") + "/tmp/test/" + fileName;
    File file = new File(localFileLocation);
    try {
      file.getParentFile().mkdirs();
      file.createNewFile();
      FileUtils.writeStringToFile(file, text, "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return file;
  }

  // Check if we can catch the console output of a script.
  @Test
  void scriptSucceeds() throws InterruptedException {
    DockerSubmissionTestModel.installImage("fedora:latest");
    // Load docker container
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("fedora");
    // Run script
    DockerTestOutput to = stm.runSubmission("echo 'PUSH ALLOWED' > /shared//output/testOutput");
    assertTrue(to.allowed);
    stm.cleanUp();
  }

  @Test
  void scriptFails() throws InterruptedException {
    //make sure docker image is installed
    DockerSubmissionTestModel.installImage("fedora:latest");
    // Load docker container
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("fedora");
    // Run script
    // Example for running a bash script correctly
    DockerTestOutput to = stm.runSubmission("echo 'PUSH DENIED' > /shared/output/testOutput");
    assertFalse(to.allowed);
    stm.cleanUp();
  }

  @Test
  void catchesConsoleLogs() throws InterruptedException {
    DockerSubmissionTestModel.installImage("alpine:latest");
    // Load docker container
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");
    // Run script
    // Example for running a bash script correctly
    DockerTestOutput to = stm.runSubmission(
        "echo 'Woopdie Woop Scoop! ~ KW'; echo 'PUSH ALLOWED' > /shared/output/testOutput");

    assertTrue(to.allowed);
    assertEquals(to.logs.get(0), "Woopdie Woop Scoop! ~ KW\n");
    stm.cleanUp();
  }

  @Test
  void correctlyReceivesInputFiles() throws InterruptedException {
    DockerSubmissionTestModel.installImage("alpine:latest");
    // Load docker container
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");

    // Create an input file in tmp/test/input
    File file = initTestFile("This is a test input file\n", "testInput");
    stm.addInputFiles(new File[]{file});
    // Run script
    // Example for running a bash script correctly
    DockerTestOutput to = stm.runSubmission(
        "cat /shared/input/testInput; echo PUSH ALLOWED > /shared/output/testOutput");
    assertEquals(to.logs.get(0), "This is a test input file\n");
    stm.cleanUp();
  }

  @Test
  void templateTest() throws InterruptedException {
    String testOne = "@HelloWorld\n" +
        ">Description=\"Test for hello world!\"\n" +
        ">Required\n" +
        "HelloWorld!\n";
    String testTwo = "@HelloWorld2\n" +
        ">Optional\n" +
        "HelloWorld2!\n";
    String template = testOne + "\n" + testTwo + "\n";

    File[] files = new File[]{initTestFile("#!/bin/sh\necho 'HelloWorld!'", "HelloWorld.sh"),
        initTestFile("#!/bin/sh\necho 'HelloWorld2!'", "HelloWorld2.sh")};

    String script =
        "chmod +x /shared/input/HelloWorld.sh;" +
            "chmod +x /shared/input/HelloWorld2.sh;" +
            "/shared/input/HelloWorld.sh > /shared/output/HelloWorld;" +
            "/shared/input/HelloWorld2.sh > /shared/output/HelloWorld2";

    DockerSubmissionTestModel.installImage("alpine:latest");
    // Load docker container
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine:latest");
    stm.addInputFiles(files);
    DockerTemplateTestOutput result = stm.runSubmissionWithTemplate(script, template);

    // Extract subtests
    List<DockerSubtestResult> results = result.getSubtestResults();

    stm.cleanUp();

    // Testing for the template parser capabilities
    assertEquals(results.size(), 2);

    assertTrue(results.get(0).isRequired());
    assertFalse(results.get(1).isRequired());

    assertEquals(results.get(0).getCorrect(), "HelloWorld!\n");
    assertEquals(results.get(1).getCorrect(), "HelloWorld2!\n");

    assertEquals(results.get(0).getTestDescription(), "Test for hello world!");
    assertEquals(results.get(1).getTestDescription(), "");

    // Test  the docker output
    assertEquals(results.get(0).getOutput(), "HelloWorld!\n");
    assertEquals(results.get(1).getOutput(), "HelloWorld2!\n");

    assertTrue(result.isAllowed());
  }

  @Test
  void artifactTest() throws IOException {
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine:latest");
    String script =
        "echo 'HelloWorld!' > /shared/artifacts/HelloWorld";

    DockerTestOutput to = stm.runSubmission(script);
    assertFalse(to.allowed);
    // check file properties
    List<File> files = stm.getArtifacts();
    assertEquals(files.size(), 1);
    assertEquals(files.get(0).getName(), "HelloWorld");
    // check file contents
    assertEquals("HelloWorld!\n", FileUtils.readFileToString(files.get(0), "UTF-8"));
    stm.cleanUp();
  }

  @Test
  void zipFileInputTest() throws IOException {
    // construct zip with hello world contents
    String sb = "Hello Happy World!";

    File f = new File("src/test/test-cases/DockerSubmissionTestTest/d__test.zip");
    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
    ZipEntry e = new ZipEntry("helloworld.txt");
    out.putNextEntry(e);

    byte[] data = sb.getBytes();
    out.write(data, 0, data.length);
    out.closeEntry();
    out.close();

    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine:latest");
    // get zipfile
    stm.addZipInputFiles(new ZipFile(f));
    DockerTestOutput output = stm.runSubmission("cat /shared/input/helloworld.txt");
    // run and check if zipfile was properly received
    assertEquals( "Hello Happy World!", output.logs.get(0));
    stm.cleanUp();

  }
  @Test
  void dockerImageDoesNotExist(){
    assertFalse(DockerSubmissionTestModel.imageExists("BADUBADUBADUBADUBADUBADUB - miauw :3"));
    assertFalse(DockerSubmissionTestModel.imageExists("alpine:v69696969"));
    assertTrue(DockerSubmissionTestModel.imageExists("alpine:latest"));
  }

  @Test
  void tryTemplate(){
    assertThrows(IllegalArgumentException.class,() -> DockerSubmissionTestModel.tryTemplate("This is not a valid template"));


    assertDoesNotThrow(() -> DockerSubmissionTestModel.tryTemplate("@HelloWorld\n" +
        ">Description=\"Test for hello world!\"\n" +
        ">Required\n" +
        "HelloWorld!"));
    assertDoesNotThrow(() -> DockerSubmissionTestModel.tryTemplate("@helloworld\n"
        + ">required\n"
        + ">description=\"Helloworldtest\"\n"
        + "Hello World\n"
        + "\n"
        + "@helloworld2\n"
        + "bruh\n"));
  }

  @Test
  void testDockerReceivesUtilFiles(){
    DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine:latest");
    Path zipLocation = Path.of("src/test/test-cases/DockerSubmissionTestTest/d__test.zip"); // simple zip with one file
    Path zipLocation2 = Path.of("src/test/test-cases/DockerSubmissionTestTest/helloworld.zip"); // complicated zip with multiple files and folder structure
    stm.addUtilFiles(zipLocation);
    stm.addUtilFiles(zipLocation2);
    DockerTestOutput to = stm.runSubmission("find /shared/extra/");
    List<String> logs = to.logs.stream().map(log -> log.replaceAll("\n", "")).sorted().toList();
    assertEquals("/shared/extra/", logs.get(0));
    assertEquals("/shared/extra/helloworld", logs.get(1));
    assertEquals("/shared/extra/helloworld.txt", logs.get(2));
    assertEquals("/shared/extra/helloworld/emptyfolder", logs.get(3));
    assertEquals("/shared/extra/helloworld/helloworld1.txt", logs.get(4));
    assertEquals("/shared/extra/helloworld/helloworld2.txt", logs.get(5)); // I don't understand the order of find :sob: but it is important all files are found.
    assertEquals("/shared/extra/helloworld/helloworld3.txt", logs.get(6));
    stm.cleanUp();
  }

}
