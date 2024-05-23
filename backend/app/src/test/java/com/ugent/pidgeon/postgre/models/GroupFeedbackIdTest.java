package com.ugent.pidgeon.postgre.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupFeedbackIdTest {

  private GroupFeedbackId groupFeedbackId;

  @BeforeEach
  public void setUp() {
    groupFeedbackId = new GroupFeedbackId();
  }

  @Test
  public void testGroupId() {
    long groupId = 1L;
    groupFeedbackId.setGroupId(groupId);
    assertEquals(groupId, groupFeedbackId.getGroupId());
  }

  @Test
  public void testProjectId() {
    long projectId = 1L;
    groupFeedbackId.setProjectId(projectId);
    assertEquals(projectId, groupFeedbackId.getProjectId());
  }

  @Test
  public void testConstructor() {
    long groupId = 1L;
    long projectId = 1L;
    GroupFeedbackId groupFeedback = new GroupFeedbackId(groupId, projectId);
    assertEquals(groupId, groupFeedback.getGroupId());
    assertEquals(projectId, groupFeedback.getProjectId());
  }
}