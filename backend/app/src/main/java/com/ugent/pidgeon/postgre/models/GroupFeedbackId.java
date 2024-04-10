package com.ugent.pidgeon.postgre.models;

import java.io.Serializable;

public class GroupFeedbackId implements Serializable {
    private long groupId;
    private long projectId;

    public GroupFeedbackId() {
    }

    public GroupFeedbackId(long groupId, long projectId) {
        this.groupId = groupId;
        this.projectId = projectId;
    }


    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }
}
