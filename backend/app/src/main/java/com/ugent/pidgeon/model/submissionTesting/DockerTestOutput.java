package com.ugent.pidgeon.model.submissionTesting;

import java.util.List;

public class DockerTestOutput {
    public List<String> logs;
    public Boolean allowed;

    public DockerTestOutput(List<String> logs, Boolean allowed) {
        this.logs = logs;
        this.allowed = allowed;
    }

}
