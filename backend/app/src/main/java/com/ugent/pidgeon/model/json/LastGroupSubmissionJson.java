package com.ugent.pidgeon.model.json;

public class LastGroupSubmissionJson {
    private String groupUrl;
    private String submissionUrl;

    public LastGroupSubmissionJson() {
    }

    public LastGroupSubmissionJson(String groupUrl, String submissionUrl) {
        this.groupUrl = groupUrl;
        this.submissionUrl = submissionUrl;
    }

    public String getGroupUrl() {
        return groupUrl;
    }

    public void setGroupUrl(String groupUrl) {
        this.groupUrl = groupUrl;
    }

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;
    }
}
