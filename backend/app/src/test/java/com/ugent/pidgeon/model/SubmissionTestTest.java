package com.ugent.pidgeon.model;

import com.ugent.pidgeon.model.submissionTesting.AddDockerModel;
import com.ugent.pidgeon.model.submissionTesting.SubmissionTestModel;
import org.junit.jupiter.api.Test;

public class SubmissionTestTest {


    // Check if we can catch the console output of a script.
    @Test
    void scriptSucceeds() throws InterruptedException {
        AddDockerModel adm = new AddDockerModel();
        // Install docker image if not already installed
        adm.addDocker("fedora:latest");
        // Load docker container
        SubmissionTestModel stm = new SubmissionTestModel("fedora");
        // Run script
        // Example for running a bash script correctly
        String[] script = {"bash", "-c", "echo 'PUSH ALLOWED' > /output/testOutput"};
        SubmissionTestModel.TestOutput to = stm.runSubmission(script);
    }

    @Test
    void scriptFails() throws InterruptedException {
        AddDockerModel adm = new AddDockerModel();
        // Install docker image if not already installed
        adm.addDocker("fedora:latest");
        // Load docker container
        SubmissionTestModel stm = new SubmissionTestModel("fedora");
        // Run script
        String[] script = {"bash", "-c", "echo 'PUSH DENIED' > /output/testOutput"};
        SubmissionTestModel.TestOutput to = stm.runSubmission(script);
    }


}
