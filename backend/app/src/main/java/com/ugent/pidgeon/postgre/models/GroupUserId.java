package com.ugent.pidgeon.postgre.models;

import java.io.Serializable;

// Hulpklasse zodat de repository correct met meerdere primary keys kan werken.
public class GroupUserId implements Serializable {
    private long groupId;
    private long userId;

    public GroupUserId(long groupId, long userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    public GroupUserId() {
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
