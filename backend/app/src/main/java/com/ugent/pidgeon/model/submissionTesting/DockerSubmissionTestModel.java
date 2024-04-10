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

public class DockerSubmissionTestModel {

    private final String localMountFolder;
    private final DockerClient dockerClient;
    private final CreateContainerCmd container;

    public DockerSubmissionTestModel(String dockerImage) {
        dockerClient = DockerClientInstance.getInstance();

        // Initialize container with a Docker image
        container = dockerClient.createContainerCmd(dockerImage);

        // Create Container Response to get the Container ID
        CreateContainerResponse response = container.exec();
        String containerID = response.getId();

        // Setup volume and mount folder
        String containerSharedFolder = "/shared"; // Folder in container to share files
        Volume sharedVolume = new Volume(containerSharedFolder);

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

    public DockerTestOutput runSubmission(String script) throws InterruptedException {
        return runSubmission(script, new File[0]);
    }

    private void copyInput(File[] inputFiles){
        for(File file : inputFiles){
            try {
                FileUtils.copyFileToDirectory(file, new File(localMountFolder + "input/"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DockerTestOutput runSubmission(String script, File[] inputFiles) throws InterruptedException {

        copyInput(inputFiles);

        // Configure and start the container
        container.withCmd("/bin/sh", "-c", script);
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
            BufferedReader reader = new BufferedReader(new FileReader(localMountFolder + "output/" + outputFileName));
            String currentLine = reader.readLine();
            allowPush = "PUSH ALLOWED".equalsIgnoreCase(currentLine);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cleanup
        removeFolder();
        dockerClient.removeContainerCmd(executionContainerID).exec();
        return new DockerTestOutput(consoleLogs, allowPush);
    }

    public List<DockerSubtestResult> runSubmissionWithTemplate(String script, String template, File[] inputFiles) throws InterruptedException {

        copyInput(inputFiles);

        // Configure and start the container
        container.withCmd("/bin/sh", "-c", script);
        CreateContainerResponse responseContainer = container.exec();
        String executionContainerID = responseContainer.getId(); // Use correct ID for operations
        dockerClient.startContainerCmd(executionContainerID).exec();

        try (ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {}) {
            dockerClient.logContainerCmd(executionContainerID)
                    .exec(callback)
                    .awaitCompletion();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<DockerSubtestResult> results = new ArrayList<>();

        // Parse template and check output
        String[] templateEntries = template.split("^@"); // start of a new line with the @ sign
        for(String entry : templateEntries){

            DockerSubtestResult templateEntry = new DockerSubtestResult();
            String[] options = entry.split("\n");
            templateEntry.setTestName(options[0]);
            int i = 1;
            for(;i < options.length; i++){
                if(options[i].charAt(0) != '>') {
                    break;
                }
                switch(options[i]){
                    case "Description":
                        templateEntry.setTestDescription(options[i].split("=\"")[1].split("\"")[0]);
                        break;
                    case "Required":
                        templateEntry.setRequired(true);
                        break;
                    case "Optional":
                        templateEntry.setRequired(false);
                        break;
                }
            }
            String[] remainingStrings = new String[options.length - i];
            System.arraycopy(options, i, remainingStrings, 0, options.length - i);
            String remaining = String.join("\n", remainingStrings);
            templateEntry.setCorrect(remaining);
            results.add(templateEntry);
        }

        for(DockerSubtestResult result : results){
            try {
                File outputFile = new File(localMountFolder + "output/" + result.getTestName());
                String currentLine = FileUtils.readFileToString(outputFile, "utf-8");
                result.setOutput(currentLine);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String outputFileName = "testOutput";
            BufferedReader reader = new BufferedReader(new FileReader(localMountFolder + "output/" + outputFileName));
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Cleanup
        removeFolder();
        dockerClient.removeContainerCmd(executionContainerID).exec();

        return new ArrayList<>();
    }





    public static void addDocker(String imageName){
        DockerClient dockerClient = DockerClientInstance.getInstance();

        // Pull the Docker image (if not already present)
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion();
        } catch (InterruptedException e) {
            System.out.println("Failed pulling docker image: " + e.getMessage());
        }
    }

    public static void removeDocker(String imageName){
        // BE SURE TO CHECK IF IMAGE IS IN USE BEFORE REMOVING
        DockerClient dockerClient = DockerClientInstance.getInstance();
        try {
            dockerClient.removeImageCmd(imageName).exec();
        } catch (Exception e) {
            System.out.println("Failed removing docker image: " + e.getMessage());
        }
    }
}
