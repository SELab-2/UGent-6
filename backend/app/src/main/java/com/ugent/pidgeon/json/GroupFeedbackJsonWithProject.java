package com.ugent.pidgeon.json;

public class GroupFeedbackJsonWithProject {

  private String projectName;
  private String projectUrl;
  private GroupFeedbackJson groupFeedback;

  private Integer maxScore;
  private Long projectId;


  public GroupFeedbackJsonWithProject(String projectName, String projectUrl, Long projectId,
      GroupFeedbackJson groupFeedback, Integer maxScore) {
    this.projectName = projectName;
    this.projectUrl = projectUrl;
    this.groupFeedback = groupFeedback;
    this.maxScore = maxScore;
    this.projectId = projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getProjectUrl() {
    return projectUrl;
  }

  public void setProjectUrl(String projectUrl) {
    this.projectUrl = projectUrl;
  }

  public GroupFeedbackJson getGroupFeedback() {
    return groupFeedback;
  }

  public void setGroupFeedback(GroupFeedbackJson groupFeedback) {
    this.groupFeedback = groupFeedback;
  }

  public Integer getMaxScore() {
      return maxScore;
  }

  public void setMaxScore(Integer maxScore) {
      this.maxScore = maxScore;
  }

  public Long getProjectId() {
      return projectId;
  }

  public void setProjectId(Long projectId) {
      this.projectId = projectId;
  }
}
