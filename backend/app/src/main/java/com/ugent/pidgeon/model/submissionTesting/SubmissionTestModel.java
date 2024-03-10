package com.ugent.pidgeon.model.submissionTesting;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.ugent.selab2.util.DockerClientInstance;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SubmissionTestModel {
    public static class TestOutput {
        public List<String> logs;
        public Boolean allowed;

        public TestOutput(List<String> logs, Boolean allowed) {
            this.logs = logs;
            this.allowed = allowed;
        }
    }

    private String localMountFolder;
    private final DockerClient dockerClient;
    private CreateContainerCmd container;
    private String containerID;

    public SubmissionTestModel(String dockerImage) {
        dockerClient = DockerClientInstance.getInstance();

        // Initialize container with a Docker image
        container = dockerClient.createContainerCmd(dockerImage);

        // Create Container Response to get the Container ID
        CreateContainerResponse response = container.exec();
        containerID = response.getId();

        // Setup volume and mount folder
        String containerOutputFolder = "/output/";
        Volume sharedVolume = new Volume(containerOutputFolder);
        String localMountFolderPrefix = "/tmp/output";
        localMountFolder = localMountFolderPrefix + containerID + "/";

        createFolder(); // Create the folder after we have the container ID

        // Configure container with volume bindings
        container.withHostConfig(new HostConfig().withBinds(new Bind(localMountFolder, sharedVolume)));
    }

    private void createFolder() { // create shared folder
        new File(localMountFolder).mkdirs();
    }

    private void removeFolder() { // clear shared folder
        try {
            FileUtils.deleteDirectory(new File(localMountFolder));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TestOutput runSubmission(String[] script) throws InterruptedException {
        // Configure and start the container
        container.withCmd(script);
        CreateContainerResponse responseContainer = container.exec();
        String executionContainerID = responseContainer.getId(); // Use correct ID for operations
        dockerClient.startContainerCmd(executionContainerID).exec();

        List<String> consoleLogs = new ArrayList<>();
        // Fetch logs
        dockerClient.logContainerCmd(executionContainerID)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .withTailAll()
                .exec(new LogContainerResultCallback() {
                    @Override
                    public void onNext(com.github.dockerjava.api.model.Frame item) {
                        consoleLogs.add(new String(item.getPayload()));
                    }
                })
                .awaitCompletion();

        boolean allowPush = false;

        try {
            String outputFileName = "testOutput";
            BufferedReader reader = new BufferedReader(new FileReader(localMountFolder + outputFileName));
            String currentLine = reader.readLine();
            allowPush = "PUSH ALLOWED".equalsIgnoreCase(currentLine);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cleanup
        removeFolder();
        dockerClient.removeContainerCmd(executionContainerID).exec();

        return new TestOutput(consoleLogs, allowPush);
    }
}
