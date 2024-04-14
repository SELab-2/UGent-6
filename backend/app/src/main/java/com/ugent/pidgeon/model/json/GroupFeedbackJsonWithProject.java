package com.ugent.pidgeon.model.json;

public class GroupFeedbackJsonWithProject {

  private String projectName;
  private String projectUrl;
  private GroupFeedbackJson groupFeedback;


  public GroupFeedbackJsonWithProject(String projectName, String projectUrl,
      GroupFeedbackJson groupFeedback) {
    this.projectName = projectName;
    this.projectUrl = projectUrl;
    this.groupFeedback = groupFeedback;
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
}
