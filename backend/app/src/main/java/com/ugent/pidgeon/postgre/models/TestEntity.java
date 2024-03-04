package com.ugent.pidgeon.postgre.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tests")
public class TestEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id", nullable = false)
    private long id;

    @Column(name = "docker_image")
    private String dockerImage;

    @Column(name = "file_test_id")
    private long fileTestId;

    public TestEntity() {
    }

    public TestEntity(String dockerImage, long fileTestId) {
        this.dockerImage = dockerImage;
        this.fileTestId = fileTestId;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public long getFileTestId() {
        return fileTestId;
    }

    public void setFileTestId(long fileTestId) {
        this.fileTestId = fileTestId;
    }
}
