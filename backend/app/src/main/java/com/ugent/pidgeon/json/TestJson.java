package com.ugent.pidgeon.json;

public class TestJson {

    private String projectUrl;
    private String dockerImage;
    private String dockerScript;
    private String dockerTemplate;
    private String structureTest;
    private String extraFilesUrl;
    private String extraFilesName;


    public TestJson() {
    }

    public TestJson(String projectUrl, String dockerImage, String dockerScript,
        String dockerTemplate, String structureTest, String extraFilesUrl, String extraFilesName) {
        this.projectUrl = projectUrl;
        this.dockerImage = dockerImage;
        this.dockerScript = dockerScript;
        this.dockerTemplate = dockerTemplate;
        this.structureTest = structureTest;
        this.extraFilesUrl = extraFilesUrl;
        this.extraFilesName = extraFilesName;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
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

    public String getStructureTest() {
        return structureTest;
    }

    public void setStructureTest(String structureTest) {
        this.structureTest = structureTest;
    }

    public String getDockerTemplate() {
        return dockerTemplate;
    }

    public void setDockerTemplate(String dockerTemplate) {
        this.dockerTemplate = dockerTemplate;
    }

    public String getExtraFilesUrl() {
        return extraFilesUrl;
    }

    public void setExtraFilesUrl(String extraFilesUrl) {
        this.extraFilesUrl = extraFilesUrl;
    }

    public String getExtraFilesName() {
        return extraFilesName;
    }

    public void setExtraFilesName(String extraFilesName) {
        this.extraFilesName = extraFilesName;
    }
}
