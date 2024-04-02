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

    @Column(name = "docker_test")
    private long dockerTestId;

    @Column(name = "structure_test_id")
    private long structureTestId;

    public TestEntity() {
    }

    public TestEntity(String dockerImage, long dockerTestId, long structureTestId) {
        this.dockerImage = dockerImage;
        this.dockerTestId = dockerTestId;
        this.structureTestId = structureTestId;
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

    public long getDockerTestId() {
        return dockerTestId;
    }

    public void setDockerTestId(long dockerTest) {
        this.dockerTestId = dockerTest;
    }

    public long getStructureTestId() {
        return structureTestId;
    }

    public void setStructureTestId(long structureTestId) {
        this.structureTestId = structureTestId;
    }
}
