package com.ugent.pidgeon.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;

public class DockerClientInstance {

    private static DockerClient dockerClient;

    private DockerClientInstance() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DockerClient getInstance() {
        if (dockerClient == null) {
            dockerClient = DockerClientBuilder.getInstance().withDockerCmdExecFactory(new NettyDockerCmdExecFactory()).build();
        }
        return dockerClient;
    }
}
