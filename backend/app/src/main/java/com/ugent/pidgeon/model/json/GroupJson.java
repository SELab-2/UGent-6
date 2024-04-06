package com.ugent.pidgeon.model.json;

import java.util.List;

public class GroupJson {

    private Long groupid;
    private String name;
    private String groupClusterUrl;

    private List<UserReferenceJson> members;

    public GroupJson() {
    }

    public GroupJson(Long groupid, String name, String groupClusterUrl) {
        this.groupid = groupid;
        this.name = name;
        this.groupClusterUrl = groupClusterUrl;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getGroupid() {
        return groupid;
    }

    public void setGroupid(Long groupid) {
        this.groupid = groupid;
    }

    public List<UserReferenceJson> getMembers() {
        return members;
    }

    public void setMembers(List<UserReferenceJson> members) {
        this.members = members;
    }

    public String getGroupClusterUrl() {
        return groupClusterUrl;
    }

    public void setGroupClusterUrl(String groupClusterUrl) {
        this.groupClusterUrl = groupClusterUrl;
    }
}
