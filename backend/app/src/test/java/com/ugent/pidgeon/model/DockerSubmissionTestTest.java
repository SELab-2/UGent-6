package com.ugent.pidgeon.model;

import com.ugent.pidgeon.model.submissionTesting.DockerSubmissionTestModel;
import com.ugent.pidgeon.model.submissionTesting.DockerTestOutput;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class DockerSubmissionTestTest {


    // Check if we can catch the console output of a script.
    @Test
    void scriptSucceeds() throws InterruptedException {
        DockerSubmissionTestModel.addDocker("fedora:latest");
        // Load docker container
        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("fedora");
        // Run script
        DockerTestOutput to = stm.runSubmission("echo 'PUSH ALLOWED' > /output/testOutput");
        assertTrue(to.allowed);
    }

    @Test
    void scriptFails() throws InterruptedException {
        //make sure docker image is installed
        DockerSubmissionTestModel.addDocker("fedora:latest");
        // Load docker container
        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("fedora");
        // Run script
        // Example for running a bash script correctly
        DockerTestOutput to = stm.runSubmission("echo 'PUSH DENIED' > /output/testOutput");
        assertFalse(to.allowed);
    }
    @Test
    void catchesConsoleLogs() throws InterruptedException {
        DockerSubmissionTestModel.addDocker("alpine:latest");
        // Load docker container
        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");
        // Run script
        // Example for running a bash script correctly
        DockerTestOutput to = stm.runSubmission("echo 'Woopdie Woop Scoop! ~ KW'; echo 'PUSH ALLOWED' > /output/testOutput");

        assertTrue(to.allowed);
        assertEquals(to.logs.get(0), "Woopdie Woop Scoop! ~ KW\n");
    }

    @Test
    void correctlyReceivesInputFiles() throws InterruptedException {
        DockerSubmissionTestModel.addDocker("alpine:latest");
        // Load docker container
        DockerSubmissionTestModel stm = new DockerSubmissionTestModel("alpine");

        // Create a input file in tmp/test/input
        String localFileLocation = System.getProperty("user.dir") + "/tmp/test/input";
        File file = new File(localFileLocation);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileUtils.writeStringToFile(file, "This is a test input file\n", "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run script
        // Example for running a bash script correctly
        DockerTestOutput to = stm.runSubmission("cat /input/testInput > /output/testOutput", new File[]{file});
        assertEquals(to.logs.get(0), "This is a test input file\n");
    }


}
