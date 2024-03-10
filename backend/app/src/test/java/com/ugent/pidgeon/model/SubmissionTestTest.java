package com.ugent.pidgeon.model;

import com.ugent.pidgeon.model.submissionTesting.SubmissionTestModel;
import com.ugent.selab2.model.AddDockerModel;
import com.ugent.selab2.model.SubmissionTestModel;
import org.junit.jupiter.api.Test;

public class SubmissionTestTest {


    // Check if we can catch the console output of a script.
    @Test
    void scriptExecutes() throws InterruptedException {

        AddDockerModel adm = new AddDockerModel();

        // Install docker image if not already installed
        //adm.addDocker("fedora:latest");
        // Load docker container
        SubmissionTestModel stm = new SubmissionTestModel("fedora");
        // Run script
        // Example for running a bash script correctly
        String[] script = {"bash", "-c", "echo 'PUSH ALLOWED' > /output/testOutput"};
        SubmissionTestModel.TestOutput to = stm.runSubmission(script);
        to.logs.forEach(System.out::println);
        System.out.println(to.allowed);

    }



}
