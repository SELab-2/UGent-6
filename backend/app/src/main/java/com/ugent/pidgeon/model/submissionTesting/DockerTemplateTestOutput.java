package com.ugent.pidgeon.model.submissionTesting;

import java.util.List;

public class DockerTemplateTestOutput implements DockerOutput{
  private List<DockerSubtestResult> subtestResults;
  private boolean allowed;

  public List<DockerSubtestResult> getSubtestResults() {
    return subtestResults;
  }

  @Override
  public boolean isAllowed() {
    return allowed;
  }

  public DockerTemplateTestOutput(List<DockerSubtestResult> subtestResults, boolean allowed) {
    this.subtestResults = subtestResults;
    this.allowed = allowed;
  }
  @Override
  public String getFeedbackAsString(){
    //json representation of the tests
    StringBuilder feedback = new StringBuilder("{subtests: [");
    for (DockerSubtestResult subtestResult : subtestResults) {
      feedback.append(subtestResult.getFeedbackAsString()).append(",");
    }
    feedback.append("]");
    return feedback.toString();
  }
}
