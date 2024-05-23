package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupClusterEntityTest {

  private GroupClusterEntity groupClusterEntity;

  @BeforeEach
  public void setUp() {
    groupClusterEntity = new GroupClusterEntity();
  }

  @Test
  public void testId() {
    long id = 1L;
    groupClusterEntity.setId(id);
    assertEquals(id, groupClusterEntity.getId());
  }

  @Test
  public void testCourseId() {
    long courseId = 1L;
    groupClusterEntity.setCourseId(courseId);
    assertEquals(courseId, groupClusterEntity.getCourseId());
  }

  @Test
  public void testMaxSize() {
    int maxSize = 10;
    groupClusterEntity.setMaxSize(maxSize);
    assertEquals(maxSize, groupClusterEntity.getMaxSize());
  }

  @Test
  public void testName() {
    String name = "Test Cluster";
    groupClusterEntity.setName(name);
    assertEquals(name, groupClusterEntity.getName());
  }

  @Test
  public void testGroupAmount() {
    int groupAmount = 5;
    groupClusterEntity.setGroupAmount(groupAmount);
    assertEquals(groupAmount, groupClusterEntity.getGroupAmount());
  }

  @Test
  public void testCreatedAt() {
    OffsetDateTime now = OffsetDateTime.now();
    groupClusterEntity.setCreatedAt(now);
    assertEquals(now, groupClusterEntity.getCreatedAt());
  }

  @Test
  public void testConstructor() {
    long courseId = 1L;
    int maxSize = 10;
    String name = "Test Cluster";
    int groupAmount = 5;
    GroupClusterEntity groupCluster = new GroupClusterEntity(courseId, maxSize, name, groupAmount);
    assertEquals(courseId, groupCluster.getCourseId());
    assertEquals(maxSize, groupCluster.getMaxSize());
    assertEquals(name, groupCluster.getName());
    assertEquals(groupAmount, groupCluster.getGroupAmount());
  }
}