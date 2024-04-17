package com.ugent.pidgeon.model.submissionTesting;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.ugent.pidgeon.util.DockerClientInstance;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class DockerSubmissionTestModel {

  private final String localMountFolder;
  private final DockerClient dockerClient;

  private final CreateContainerCmd container;

  public DockerSubmissionTestModel(String dockerImage) {

    dockerClient = DockerClientInstance.getInstance();

    // Initialize container with a Docker image
    container = dockerClient.createContainerCmd(dockerImage);

    // Create Container Response to get the Container ID

    // Setup volume and mount folder
    String containerSharedFolder = "/shared"; // Folder in container to share files
    Volume sharedVolume = new Volume(containerSharedFolder);

    // Get temp folder of project, for mounting the container output
    String localMountFolderPrefix = System.getProperty("user.dir") + "/tmp/dockerTestOutput";

    String containerFilesId = String.valueOf(System.currentTimeMillis());
    localMountFolder = localMountFolderPrefix + containerFilesId + "/";

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

  private void runContainer(String script, File[] inputFiles, ResultCallback.Adapter<Frame> callback) {

    // Init directories in the shared folder
    new File(localMountFolder + "input/").mkdirs();
    new File(localMountFolder + "output/").mkdirs();

    // Copy input files to the shared folder
    for (File file : inputFiles) {
      try {
        FileUtils.copyFileToDirectory(file, new File(localMountFolder + "input/"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Configure and start the container
    container.withCmd("/bin/sh", "-c", script);
    CreateContainerResponse responseContainer = container.exec();
    String executionContainerID = responseContainer.getId(); // Use correct ID for operations
    dockerClient.startContainerCmd(executionContainerID).exec();
    try{
      dockerClient.logContainerCmd(executionContainerID)
          .withStdOut(true)
          .withStdErr(true)
          .withFollowStream(true)
          .withTailAll()
          .exec(callback)
          .awaitCompletion();
    }catch (InterruptedException e){
      System.err.println("Failed to read output file. Push is denied.");
    }

    // Cleanup the container
    dockerClient.removeContainerCmd(executionContainerID)
        .withForce(true)
        .withRemoveVolumes(true)
        .exec();

  }

  public DockerTestOutput runSubmission(String script, File[] inputFiles)
      {

    List<String> consoleLogs = new ArrayList<>();
    ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
      @Override
      public void onNext(Frame item) {
        consoleLogs.add(new String(item.getPayload()));
      }
    };
    runContainer(script, inputFiles, callback);

    boolean allowPush;

    try {
      String outputFileName = "testOutput";
      BufferedReader reader = new BufferedReader(
          new FileReader(localMountFolder + "output/" + outputFileName));
      String currentLine = reader.readLine();
      allowPush = "PUSH ALLOWED".equalsIgnoreCase(currentLine);
      reader.close();
    } catch (Exception e) {
      System.err.println("Failed to read output file. Push is denied.");
      allowPush = false;
    }

    // Cleanup
    removeFolder();
    return new DockerTestOutput(consoleLogs, allowPush);
  }

  public DockerTemplateTestResult runSubmissionWithTemplate(String script, String template,
      File[] inputFiles) throws InterruptedException {

    runContainer(script, inputFiles, new Adapter<>());

    // execute dockerClient and await

    List<DockerSubtestResult> results = new ArrayList<>();

    // Parse template and check output
    String[] templateEntries = template.split("@");
    /// remove first entry (empty string)
    templateEntries = Arrays.copyOfRange(templateEntries, 1, templateEntries.length);

    // start of a new line with the @ sign

    for (String entry : templateEntries) {

      DockerSubtestResult templateEntry = getDockerSubtestResult(
          entry);
      results.add(templateEntry);
    }

    for (DockerSubtestResult result : results) {
      try {
        File outputFile = new File(localMountFolder + "output/" + result.getTestName());
        String currentLine = FileUtils.readFileToString(outputFile, "utf-8");
        result.setOutput(currentLine);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Cleanup
    removeFolder();

    // Check if allowed
    boolean allowed = true;
    for (DockerSubtestResult result : results) {
      if (result.isRequired() && !result.getCorrect().equals(result.getOutput())) {
        allowed = false;
        break;
      }
    }

    return new DockerTemplateTestResult(results, allowed);
  }

  private static DockerSubtestResult getDockerSubtestResult(String entry) {
    DockerSubtestResult templateEntry = new DockerSubtestResult();

    List<String> options = new ArrayList<>();
    int lineIterator = 0;
    // parse name
    while (entry.charAt(lineIterator) != '\n') {
      lineIterator++;
    }
    // parse lines as long as there are options ( lines starting with a > )
    templateEntry.setTestName(entry.substring(0, lineIterator));
    lineIterator++;
    StringBuilder currentLine = new StringBuilder(entry.charAt(lineIterator) + "");
    while (currentLine.charAt(0) == '>') {
      lineIterator++;
      if (entry.charAt(lineIterator) == '\n') {
        options.add(currentLine.toString());
        lineIterator++;
        currentLine = new StringBuilder(entry.charAt(lineIterator) + "");
      } else {
        currentLine.append(entry.charAt(lineIterator));
      }
    }
    for (String currentOption : options) {
      if (currentOption.charAt(0) != '>') {
        break;
      }
      if (currentOption.equalsIgnoreCase(">Required")) {
        templateEntry.setRequired(true);
      } else if (currentOption.equalsIgnoreCase(">Optional")) {
        templateEntry.setRequired(false);
      } else if (currentOption.substring(0, 12).equalsIgnoreCase(">Description")) {
        templateEntry.setTestDescription(currentOption.split("=\"")[1].split("\"")[0]);
      }
    }
    templateEntry.setCorrect(entry.substring(lineIterator));
    return templateEntry;
  }

  public static void addDocker(String imageName) {
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

  public static void removeDockerImage(String imageName) {
    // BE SURE TO CHECK IF IMAGE IS IN USE BEFORE REMOVING
    DockerClient dockerClient = DockerClientInstance.getInstance();
    try {
      dockerClient.removeImageCmd(imageName).exec();
    } catch (Exception e) {
      System.out.println("Failed removing docker image: " + e.getMessage());
    }
  }
}