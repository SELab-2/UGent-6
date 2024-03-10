package com.ugent.pidgeon.model.submissionTesting;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.ugent.pidgeon.util.DockerClientInstance;

public class AddDockerModel {
    public void addDocker(String imageName){
        DockerClient dockerClient = DockerClientInstance.getInstance();

        // Pull the Docker image (if not already present)
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
        } catch (InterruptedException e) {
            System.out.println("Failed pulling docker image: " + e.getMessage());
        }
    }
}
