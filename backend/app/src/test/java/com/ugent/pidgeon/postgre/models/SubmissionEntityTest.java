package com.ugent.pidgeon.postgre.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubmissionEntityTest {

  private SubmissionEntity submissionEntity;

  @BeforeEach
  public void setUp() {
    submissionEntity = new SubmissionEntity();
  }

  @Test
  public void testId() {
    long id = 1L;
    submissionEntity.setId(id);
    assertEquals(id, submissionEntity.getId());
  }

  @Test
  public void testProjectId() {
    long projectId = 1L;
    submissionEntity.setProjectId(projectId);
    assertEquals(projectId, submissionEntity.getProjectId());
  }



  @Test
  public void testFileId() {
    long fileId = 1L;
    submissionEntity.setFileId(fileId);
    assertEquals(fileId, submissionEntity.getFileId());
  }

  @Test
  public void testSubmissionTime() {
    OffsetDateTime submissionTime = OffsetDateTime.now();
    submissionEntity.setSubmissionTime(submissionTime);
    assertEquals(submissionTime, submissionEntity.getSubmissionTime());
  }

  @Test
  public void testStructureAccepted() {
    Boolean structureAccepted = true;
    submissionEntity.setStructureAccepted(structureAccepted);
    assertEquals(structureAccepted, submissionEntity.getStructureAccepted());
  }

  @Test
  public void testDockerAccepted() {
    Boolean dockerAccepted = true;
    submissionEntity.setDockerAccepted(dockerAccepted);
    assertEquals(dockerAccepted, submissionEntity.getDockerAccepted());
  }

  @Test
  public void testStructureFeedback() {
    String structureFeedback = "Structure feedback";
    submissionEntity.setStructureFeedback(structureFeedback);
    assertEquals(structureFeedback, submissionEntity.getStructureFeedback());
  }

  @Test
  public void testDockerFeedback() {
    String dockerFeedback = "Docker feedback";
    submissionEntity.setDockerFeedback(dockerFeedback);
    assertEquals(dockerFeedback, submissionEntity.getDockerFeedback());
  }

  @Test
  public void testConstructor() {
    long projectId = 1L;
    long groupId = 1L;
    Long fileId = 1L;
    OffsetDateTime submissionTime = OffsetDateTime.now();
    Boolean structureAccepted = true;
    Boolean dockerAccepted = true;
    SubmissionEntity submission = new SubmissionEntity(projectId, groupId, fileId, submissionTime, structureAccepted, dockerAccepted);
    assertEquals(projectId, submission.getProjectId());
    assertEquals(groupId, submission.getGroupId());
    assertEquals(fileId, submission.getFileId());
    assertEquals(submissionTime, submission.getSubmissionTime());
    assertEquals(structureAccepted, submission.getStructureAccepted());
    assertEquals(dockerAccepted, submission.getDockerAccepted());
  }
}