package com.ugent.pidgeon.json;

public class TestUpdateJson {
  private String dockerImage;
  private String dockerScript;
  private String dockerTemplate;
  private String structureTest;

  public TestUpdateJson(String dockerImage, String dockerScript, String dockerTemplate, String structureTest) {
    this.dockerImage = dockerImage;
    this.dockerScript = dockerScript;
    this.dockerTemplate = dockerTemplate;
    this.structureTest = structureTest;
  }

  public String getDockerImage() {
    return dockerImage;
  }

  public void setDockerImage(String dockerImage) {
    this.dockerImage = dockerImage;
  }

  public String getDockerScript() {
    return dockerScript;
  }

  public void setDockerScript(String dockerScript) {
    this.dockerScript = dockerScript;
  }

  public String getDockerTemplate() {
    return dockerTemplate;
  }

  public void setDockerTemplate(String dockerTemplate) {
    this.dockerTemplate = dockerTemplate;
  }

  public String getStructureTest() {
    return structureTest;
  }

  public void setStructureTest(String structureTest) {
    this.structureTest = structureTest;
  }

}

