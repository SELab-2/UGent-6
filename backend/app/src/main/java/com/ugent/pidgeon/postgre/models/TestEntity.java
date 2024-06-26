package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tests")
public class TestEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id", nullable = false)
    private long id;

    @Column(name = "docker_image")
    private String dockerImage;

    @Column(name = "docker_test_script")
    private String dockerTestScript;

    @Column(name = "docker_test_template")
    private String dockerTestTemplate;

    @Column(name = "structure_template")
    private String structureTemplate;

    @Column(name = "extra_files")
    private Long extraFilesId;

    public TestEntity(String dockerImage, String docker_test_script,
        String dockerTestTemplate,
        String structureTemplate) {
        this.dockerImage = dockerImage;
        this.dockerTestScript = docker_test_script;
        this.dockerTestTemplate = dockerTestTemplate;
        this.structureTemplate = structureTemplate;
    }

    public TestEntity() {

    }

    public String getDockerTestScript() {
        return dockerTestScript;
    }

    public void setDockerTestScript(String docker_test_script) {
        this.dockerTestScript = docker_test_script;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public String getDockerTestTemplate() {
        return dockerTestTemplate;
    }

    public void setDockerTestTemplate(String dockerTestTemplate) {
        this.dockerTestTemplate = dockerTestTemplate;
    }

    public String getStructureTemplate() {
        return structureTemplate;
    }

    public void setStructureTemplate(String structureTemplate) {
        this.structureTemplate = structureTemplate;
    }

    public Long getExtraFilesId() {
        return extraFilesId;
    }

    public void setExtraFilesId(Long extraFilesId) {
        this.extraFilesId = extraFilesId;
    }
}
