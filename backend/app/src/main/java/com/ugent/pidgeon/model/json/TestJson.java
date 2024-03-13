package com.ugent.pidgeon.model.json;

public class TestJson {
    private String projectUrl;
    private String dockerImage;
    private String dokcerTestUrl;
    private String structureTestUrl;

    public TestJson() {
    }

    public TestJson(String projectUrl, String dockerImage, String dokcerTestUrl, String structureTestUrl) {
        this.projectUrl = projectUrl;
        this.dockerImage = dockerImage;
        this.dokcerTestUrl = dokcerTestUrl;
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

    public String getDokcerTestUrl() {
        return dokcerTestUrl;
    }

    public void setDokcerTestUrl(String dokcerTestUrl) {
        this.dokcerTestUrl = dokcerTestUrl;
    }

    public String getStructureTestUrl() {
        return structureTestUrl;
    }

    public void setStructureTestUrl(String structureTestUrl) {
        this.structureTestUrl = structureTestUrl;
    }
}
