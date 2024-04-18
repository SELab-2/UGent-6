package com.ugent.pidgeon.postgre.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CourseEntityTest {

  private CourseEntity courseEntity;

  @BeforeEach
  public void setUp() {
    courseEntity = new CourseEntity();
  }

  @Test
  public void testId() {
    long id = 1L;
    courseEntity.setId(id);
    assertEquals(id, courseEntity.getId());
  }

  @Test
  public void testName() {
    String name = "Test Course";
    courseEntity.setName(name);
    assertEquals(name, courseEntity.getName());
  }

  @Test
  public void testDescription() {
    String description = "Test Description";
    courseEntity.setDescription(description);
    assertEquals(description, courseEntity.getDescription());
  }

  @Test
  public void testCreatedAt() {
    OffsetDateTime now = OffsetDateTime.now();
    courseEntity.setCreatedAt(now);
    assertEquals(now, courseEntity.getCreatedAt());
  }

  @Test
  public void testJoinKey() {
    String joinKey = "Test Join Key";
    courseEntity.setJoinKey(joinKey);
    assertEquals(joinKey, courseEntity.getJoinKey());
  }

  @Test
  public void testConstructor() {
    String name = "Test Course";
    String description = "Test Description";
    CourseEntity course = new CourseEntity(name, description);
    assertEquals(name, course.getName());
    assertEquals(description, course.getDescription());
  }
}