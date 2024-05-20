package com.ugent.pidgeon.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;

public class DockerClientInstance {

  private static DockerClient dockerClient;

  private DockerClientInstance() {
    // Private constructor to prevent instantiation
  }

  // @Relevant
  public static synchronized DockerClient getInstance() {

    if (dockerClient == null) {

      DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
      DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
          .dockerHost(config.getDockerHost())
          .sslConfig(config.getSSLConfig())
          .maxConnections(100)
          .connectionTimeout(Duration.ofSeconds(30))
          .responseTimeout(Duration.ofSeconds(45))
          .build();
      dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }
    return dockerClient;
  }

}
