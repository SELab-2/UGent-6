package com.ugent.pidgeon.model.submissionTesting;

import java.util.List;
import java.util.logging.Logger;
import org.hibernate.usertype.LoggableUserType;

public class DockerTemplateTestOutput implements DockerOutput{
  private final List<DockerSubtestResult> subtestResults;
  private final boolean allowed;

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
    StringBuilder feedback = new StringBuilder("{\"subtests\": [");
    for (DockerSubtestResult subtestResult : subtestResults) {
      feedback.append(subtestResult.getFeedbackAsString())
          .append(",");
    }
    feedback.deleteCharAt(feedback.length() - 1); // remove last comma ,
    feedback.append("]}");
    Logger.getGlobal().info(feedback.toString());
    return feedback.toString();
  }
}
