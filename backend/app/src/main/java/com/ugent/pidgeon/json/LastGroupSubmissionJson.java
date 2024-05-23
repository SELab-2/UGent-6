package com.ugent.pidgeon.json;

public class LastGroupSubmissionJson {

    private SubmissionJson submission;
    private GroupJson group;
    private GroupFeedbackJson feedback;

    public LastGroupSubmissionJson() {
    }

    public LastGroupSubmissionJson(SubmissionJson submission, GroupJson group, GroupFeedbackJson feedback) {
        this.submission = submission;
        this.group = group;
        this.feedback = feedback;
    }


    public SubmissionJson getSubmission() {
        return submission;
    }

    public void setSubmission(SubmissionJson submission) {
        this.submission = submission;
    }

    public GroupJson getGroup() {
        return group;
    }

    public void setGroup(GroupJson group) {
        this.group = group;
    }

    public GroupFeedbackJson getFeedback() {
        return feedback;
    }

    public void setFeedback(GroupFeedbackJson feedback) {
        this.feedback = feedback;
    }
}
