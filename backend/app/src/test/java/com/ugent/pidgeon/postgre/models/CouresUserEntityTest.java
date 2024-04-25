package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ugent.pidgeon.postgre.models.types.CourseRelation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CouresUserEntityTest {

  private CourseUserEntity courseUserEntity;

  @BeforeEach
  public void setUp() {
    courseUserEntity = new CourseUserEntity();
  }

  @Test
  public void testCourseId() {
    long courseId = 1L;
    courseUserEntity.setCourseId(courseId);
    assertEquals(courseId, courseUserEntity.getCourseId());
  }

  @Test
  public void testUserId() {
    long userId = 1L;
    courseUserEntity.setUserId(userId);
    assertEquals(userId, courseUserEntity.getUserId());
  }

  @Test
  public void testRelation() {
    CourseRelation relation = CourseRelation.creator;
    courseUserEntity.setRelation(relation);
    assertEquals(relation, courseUserEntity.getRelation());
  }

  @Test
  public void testConstructor() {
    long courseId = 1L;
    long userId = 1L;
    CourseRelation relation = CourseRelation.creator;
    CourseUserEntity courseUser = new CourseUserEntity(courseId, userId, relation);
    assertEquals(courseId, courseUser.getCourseId());
    assertEquals(userId, courseUser.getUserId());
    assertEquals(relation, courseUser.getRelation());
  }
}
