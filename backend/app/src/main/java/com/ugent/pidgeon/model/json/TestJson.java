package com.ugent.pidgeon.model.json;

public class TestJson {
    private String projectUrl;
    private String dockerImage;
    private String dockerTestUrl;
    private String structureTestUrl;

    public TestJson() {
    }

    public TestJson(String projectUrl, String dockerImage, String dockerTestUrl, String structureTestUrl) {
        this.projectUrl = projectUrl;
        this.dockerImage = dockerImage;
        this.dockerTestUrl = dockerTestUrl;
        this.structureTestUrl = structureTestUrl;
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

    public String getDockerTestUrl() {
        return dockerTestUrl;
    }

    public void setDockerTestUrl(String dockerTestUrl) {
        this.dockerTestUrl = dockerTestUrl;
    }

    public String getStructureTestUrl() {
        return structureTestUrl;
    }

    public void setStructureTestUrl(String structureTestUrl) {
        this.structureTestUrl = structureTestUrl;
    }
}
