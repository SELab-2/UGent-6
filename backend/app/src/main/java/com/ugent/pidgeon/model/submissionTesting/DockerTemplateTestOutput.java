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
  public String toString(){
    // json representation of the tests
    String subTestsJson = "[";
    for (DockerSubtestResult subtestResult : subtestResults) {
      String subTestJson = "{" +
          "subtestName=" + subtestResult.getTestName() +
          ", allowed=" + subtestResult.getCorrect() +
          ", output=" + subtestResult.getOutput() +
          "}";
    }
    subTestsJson += "]";

    return "{" +
        "subtestResults=" + subtestResults +
        ", allowed=" + allowed +
        '}';
  }
}
