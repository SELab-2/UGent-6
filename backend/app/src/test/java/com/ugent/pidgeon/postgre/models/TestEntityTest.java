package com.ugent.pidgeon.postgre.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEntityTest {

  private TestEntity testEntity;

  @BeforeEach
  public void setUp() {
    testEntity = new TestEntity();
  }

  @Test
  public void testId() {
    long id = 1L;
    testEntity.setId(id);
    assertEquals(id, testEntity.getId());
  }

  @Test
  public void testDockerImage() {
    String dockerImage = "Docker Image";
    testEntity.setDockerImage(dockerImage);
    assertEquals(dockerImage, testEntity.getDockerImage());
  }

  @Test
  public void testDockerTestId() {
    long dockerTestId = 1L;
    testEntity.setDockerTestId(dockerTestId);
    assertEquals(dockerTestId, testEntity.getDockerTestId());
  }

  @Test
  public void testStructureTestId() {
    long structureTestId = 1L;
    testEntity.setStructureTestId(structureTestId);
    assertEquals(structureTestId, testEntity.getStructureTestId());
  }

  @Test
  public void testConstructor() {
    String dockerImage = "Docker Image";
    long dockerTestId = 1L;
    long structureTestId = 1L;
    TestEntity test = new TestEntity(dockerImage, dockerTestId, structureTestId);
    assertEquals(dockerImage, test.getDockerImage());
    assertEquals(dockerTestId, test.getDockerTestId());
    assertEquals(structureTestId, test.getStructureTestId());
  }
}