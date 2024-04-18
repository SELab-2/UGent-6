package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CouresUserIdTest {

  private CourseUserId courseUserId;

  @BeforeEach
  public void setUp() {
    courseUserId = new CourseUserId();
  }

  @Test
  public void testCourseId() {
    long courseId = 1L;
    courseUserId.setCourseId(courseId);
    assertEquals(courseId, courseUserId.getCourseId());
  }

  @Test
  public void testUserId() {
    long userId = 1L;
    courseUserId.setUserId(userId);
    assertEquals(userId, courseUserId.getUserId());
  }
}
