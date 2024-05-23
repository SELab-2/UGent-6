package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupEntityTest {

  private GroupEntity groupEntity;

  @BeforeEach
  public void setUp() {
    groupEntity = new GroupEntity();
  }

  @Test
  public void testId() {
    long id = 1L;
    groupEntity.setId(id);
    assertEquals(id, groupEntity.getId());
  }

  @Test
  public void testName() {
    String name = "Test Group";
    groupEntity.setName(name);
    assertEquals(name, groupEntity.getName());
  }

  @Test
  public void testClusterId() {
    long clusterId = 1L;
    groupEntity.setClusterId(clusterId);
    assertEquals(clusterId, groupEntity.getClusterId());
  }

  @Test
  public void testConstructor() {
    String name = "Test Group";
    long clusterId = 1L;
    GroupEntity group = new GroupEntity(name, clusterId);
    assertEquals(name, group.getName());
    assertEquals(clusterId, group.getClusterId());
  }
}