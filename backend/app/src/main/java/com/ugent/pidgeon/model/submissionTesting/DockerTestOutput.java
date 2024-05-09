package com.ugent.pidgeon.model.submissionTesting;

import java.util.List;

public class DockerTestOutput implements DockerOutput {
    public List<String> logs;
    public Boolean allowed;

    public DockerTestOutput(List<String> logs, Boolean allowed) {
        this.logs = logs;
        this.allowed = allowed;
    }

    @Override
    public boolean isAllowed() {
        return allowed;
    }

    @Override
    public String getFeedbackAsString() {
        return String.join("", logs);
    }
}
