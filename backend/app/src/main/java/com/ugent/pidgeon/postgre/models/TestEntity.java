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
    private long dockerImage;

    @Column(name = "docker_test")
    private long dockerTest;

    @Column(name = "structure_test_id")
    private long structureTestId;

    public TestEntity() {
    }

    public TestEntity(long dockerImage, long dockerTest, long structureTestId) {
        this.dockerImage = dockerImage;
        this.dockerTest = dockerTest;
        this.structureTestId = structureTestId;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public long getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(long dockerImage) {
        this.dockerImage = dockerImage;
    }

    public long getDockerTest() {
        return dockerTest;
    }

    public void setDockerTest(long dockerTest) {
        this.dockerTest = dockerTest;
    }

    public long getStructureTestId() {
        return structureTestId;
    }

    public void setStructureTestId(long structureTestId) {
        this.structureTestId = structureTestId;
    }
}
