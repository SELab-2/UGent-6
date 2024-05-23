package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProjectEntityTest {

  private ProjectEntity projectEntity;

  @BeforeEach
  public void setUp() {
    projectEntity = new ProjectEntity();
  }

  @Test
  public void testId() {
    long id = 1L;
    projectEntity.setId(id);
    assertEquals(id, projectEntity.getId());
  }

  @Test
  public void testCourseId() {
    long courseId = 1L;
    projectEntity.setCourseId(courseId);
    assertEquals(courseId, projectEntity.getCourseId());
  }

  @Test
  public void testName() {
    String name = "Test Project";
    projectEntity.setName(name);
    assertEquals(name, projectEntity.getName());
  }

  @Test
  public void testDescription() {
    String description = "This is a test project.";
    projectEntity.setDescription(description);
    assertEquals(description, projectEntity.getDescription());
  }

  @Test
  public void testGroupClusterId() {
    long groupClusterId = 1L;
    projectEntity.setGroupClusterId(groupClusterId);
    assertEquals(groupClusterId, projectEntity.getGroupClusterId());
  }

  @Test
  public void testTestId() {
    Long testId = 1L;
    projectEntity.setTestId(testId);
    assertEquals(testId, projectEntity.getTestId());
  }

  @Test
  public void testVisible() {
    Boolean visible = true;
    projectEntity.setVisible(visible);
    assertEquals(visible, projectEntity.isVisible());
  }

  @Test
  public void testMaxScore() {
    Integer maxScore = 100;
    projectEntity.setMaxScore(maxScore);
    assertEquals(maxScore, projectEntity.getMaxScore());
  }

  @Test
  public void testDeadline() {
    OffsetDateTime deadline = OffsetDateTime.now();
    projectEntity.setDeadline(deadline);
    assertEquals(deadline, projectEntity.getDeadline());
  }

  @Test
  public void testConstructor() {
    long courseId = 1L;
    String name = "Test Project";
    String description = "This is a test project.";
    long groupClusterId = 1L;
    Long testId = 1L;
    Boolean visible = true;
    Integer maxScore = 100;
    OffsetDateTime deadline = OffsetDateTime.now();
    ProjectEntity project = new ProjectEntity(courseId, name, description, groupClusterId, testId, visible, maxScore, deadline);
    assertEquals(courseId, project.getCourseId());
    assertEquals(name, project.getName());
    assertEquals(description, project.getDescription());
    assertEquals(groupClusterId, project.getGroupClusterId());
    assertEquals(testId, project.getTestId());
    assertEquals(visible, project.isVisible());
    assertEquals(maxScore, project.getMaxScore());
    assertEquals(deadline, project.getDeadline());
  }
}