package com.ugent.pidgeon.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.DockerSubtestResult;
import com.ugent.pidgeon.model.submissionTesting.DockerTemplateTestResult;
import com.ugent.pidgeon.model.submissionTesting.DockerTestOutput;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class DockerSubmissionTestTest {

//    File initTestFile(String text, String fileName) {
//        String localFileLocation = System.getProperty("user.dir") + "/tmp/test/" + fileName;
//        File file = new File(localFileLocation);
//        try {
//            file.getParentFile().mkdirs();
//            file.createNewFile();
//            FileUtils.writeStringToFile(file, text, "UTF-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return file;
//    }
//
//    // Check if we can catch the console output of a script.
//    @Test
//    void scriptSucceeds() throws InterruptedException {
//        DockerSubmissionTestModel.addDocker("fedora:latest");
//        // Load docker container
//        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("fedora");
//        // Run script
//        DockerTestOutput to = stm.runSubmission("echo 'PUSH ALLOWED' > /shared//output/testOutput");
//        assertTrue(to.allowed);
//    }
//
//    @Test
//    void scriptFails() throws InterruptedException {
//        //make sure docker image is installed
//        DockerSubmissionTestModel.addDocker("fedora:latest");
//        // Load docker container
//        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("fedora");
//        // Run script
//        // Example for running a bash script correctly
//        DockerTestOutput to = stm.runSubmission("echo 'PUSH DENIED' > /shared/output/testOutput");
//        assertFalse(to.allowed);
//    }
//
//    @Test
//    void catchesConsoleLogs() throws InterruptedException {
//        DockerSubmissionTestModel.addDocker("alpine:latest");
//        // Load docker container
//        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");
//        // Run script
//        // Example for running a bash script correctly
//        DockerTestOutput to = stm.runSubmission("echo 'Woopdie Woop Scoop! ~ KW'; echo 'PUSH ALLOWED' > /shared/output/testOutput");
//
//        assertTrue(to.allowed);
//        assertEquals(to.logs.get(0), "Woopdie Woop Scoop! ~ KW\n");
//    }
//
//    @Test
//    void correctlyReceivesInputFiles() throws InterruptedException {
//        DockerSubmissionTestModel.addDocker("alpine:latest");
//        // Load docker container
//        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");
//
//        // Create an input file in tmp/test/input
//        File file = initTestFile("This is a test input file\n", "testInput");
//
//        // Run script
//        // Example for running a bash script correctly
//        DockerTestOutput to = stm.runSubmission("cat /shared/input/testInput; echo PUSH ALLOWED > /shared/output/testOutput", new File[]{file});
//        assertEquals(to.logs.get(0), "This is a test input file\n");
//    }
//
//    @Test
//    void templateTest() throws InterruptedException {
//        String testOne = "@HelloWorld\n" +
//                ">Description=\"Test for hello world!\"\n" +
//                ">Required\n" +
//                "HelloWorld!";
//        String testTwo = "@HelloWorld2\n" +
//                ">Optional\n" +
//                "HelloWorld2!\n";
//        String template = testOne + "\n" + testTwo;
//
//        File[] files = new File[]{initTestFile("#!/bin/sh\necho 'HelloWorld!'", "HelloWorld.sh"),
//                initTestFile("#!/bin/sh\necho 'HelloWorld2!'", "HelloWorld2.sh")};
//
//        String script =
//                "chmod +x /shared/input/HelloWorld.sh;" +
//                "chmod +x /shared/input/HelloWorld2.sh;" +
//                "/shared/input/HelloWorld.sh > /shared/output/HelloWorld;" +
//                "/shared/input/HelloWorld2.sh > /shared/output/HelloWorld2";
//
//        DockerSubmissionTestModel.addDocker("alpine:latest");
//        // Load docker container
//        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");
//        DockerTemplateTestResult result = stm.runSubmissionWithTemplate(script, template, files);
//
//        // Extract subtests
//        List<DockerSubtestResult> results = result.getSubtestResults();
//
//        // Testing for the template parser capabilities
//        assertEquals(results.size(), 2);
//
//        assertTrue(results.get(0).isRequired());
//        assertFalse(results.get(1).isRequired());
//
//        assertEquals(results.get(0).getCorrect(), "HelloWorld!\n");
//        assertEquals(results.get(1).getCorrect(), "HelloWorld2!\n");
//
//        assertEquals(results.get(0).getTestDescription(), "Test for hello world!");
//        assertEquals(results.get(1).getTestDescription(), "");
//
//        // Test  the docker output
//        assertEquals(results.get(0).getOutput(), "HelloWorld!\n");
//        assertEquals(results.get(1).getOutput(), "HelloWorld2!\n");
//
//        assertTrue(result.isAllowed());
//
//    }


}
