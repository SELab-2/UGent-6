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
  public void testDockerTestScript() {
    String dockerTestScript = "Docker Test Script";
    testEntity.setDockerTestScript(dockerTestScript);
    assertEquals(dockerTestScript, testEntity.getDockerTestScript());
  }

  @Test
  public void testStructureTestId() {
    String template = "@Testone\nHello World!";
    testEntity.setStructureTemplate(template);
    assertEquals(template, testEntity.getDockerTestScript());
  }

  @Test
  public void testConstructor() {
    String dockerImage = "Docker image";
    String dockerTestScript = "echo 'hello'";
    String dockerTestTemplate = "@testone\nHello World!";
    String structureTestId = "src/";

    TestEntity test = new TestEntity(dockerImage, dockerTestScript, dockerTestTemplate, structureTestId);

    assertEquals(dockerImage, test.getDockerImage());
    assertEquals(dockerTestScript, test.getDockerTestScript());
    assertEquals(dockerTestTemplate, test.getDockerTestTemplate());
    assertEquals(structureTestId, test.getStructureTemplate());

  }
}