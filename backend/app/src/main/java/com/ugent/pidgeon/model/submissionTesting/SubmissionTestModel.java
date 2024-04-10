package com.ugent.pidgeon.model.submissionTesting;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.ugent.pidgeon.util.DockerClientInstance;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    public SubmissionTestModel(String dockerImage) {
        dockerClient = DockerClientInstance.getInstance();

        // Initialize container with a Docker image
        container = dockerClient.createContainerCmd(dockerImage);

        // Create Container Response to get the Container ID
        CreateContainerResponse response = container.exec();
        String containerID = response.getId();

        // Setup volume and mount folder
        String containerOutputFolder = "/output/";
        Volume sharedVolume = new Volume(containerOutputFolder);

        // Get temp folder of project, for mounting the container output
        String localMountFolderPrefix = System.getProperty("user.dir") + "/tmp/dockerTestOutput";

        localMountFolder = localMountFolderPrefix + containerID + "/";

        createFolder(); // Create the folder after we// generate tmp folder of project
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

        try (ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame item) {
                consoleLogs.add(new String(item.getPayload()));
            }
        }) {
            dockerClient.logContainerCmd(executionContainerID)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withTailAll()
                    .exec(callback)
                    .awaitCompletion();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
