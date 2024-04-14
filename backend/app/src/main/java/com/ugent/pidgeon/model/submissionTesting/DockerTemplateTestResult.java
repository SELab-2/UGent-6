package com.ugent.pidgeon.model.submissionTesting;

import java.util.List;

public class DockerTemplateTestResult {
  private List<DockerSubtestResult> subtestResults;
  private boolean allowed;

  public List<DockerSubtestResult> getSubtestResults() {
    return subtestResults;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public DockerTemplateTestResult(List<DockerSubtestResult> subtestResults, boolean allowed) {
    this.subtestResults = subtestResults;
    this.allowed = allowed;
  }
}
