package com.ugent.pidgeon.postgre.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupFeedbackEntityTest {

  private GroupFeedbackEntity groupFeedbackEntity;

  @BeforeEach
  public void setUp() {
    groupFeedbackEntity = new GroupFeedbackEntity();
  }

  @Test
  public void testGroupId() {
    long groupId = 1L;
    groupFeedbackEntity.setGroupId(groupId);
    assertEquals(groupId, groupFeedbackEntity.getGroupId());
  }

  @Test
  public void testProjectId() {
    long projectId = 1L;
    groupFeedbackEntity.setProjectId(projectId);
    assertEquals(projectId, groupFeedbackEntity.getProjectId());
  }

  @Test
  public void testGrade() {
    float grade = 85.0f;
    groupFeedbackEntity.setScore(grade);
    assertEquals(grade, groupFeedbackEntity.getScore());
  }

  @Test
  public void testFeedback() {
    String feedback = "Good job!";
    groupFeedbackEntity.setFeedback(feedback);
    assertEquals(feedback, groupFeedbackEntity.getFeedback());
  }

  @Test
  public void testConstructor() {
    long groupId = 1L;
    long projectId = 1L;
    float grade = 85.0f;
    String feedback = "Good job!";
    GroupFeedbackEntity groupFeedback = new GroupFeedbackEntity(groupId, projectId, grade, feedback);
    assertEquals(groupId, groupFeedback.getGroupId());
    assertEquals(projectId, groupFeedback.getProjectId());
    assertEquals(grade, groupFeedback.getScore());
    assertEquals(feedback, groupFeedback.getFeedback());
  }
}