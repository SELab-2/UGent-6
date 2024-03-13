package com.ugent.pidgeon.model.json;

import com.ugent.pidgeon.postgre.models.GroupEntity;

import java.util.List;

public class GroupJson {
    private String name;
    private String groupCluster;

    private List<UserReferenceJson> members;

    public GroupJson() {
    }

    public GroupJson(String name, String groupClusterUrl) {
        this.name = name;
        this.groupCluster = groupClusterUrl;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public List<UserReferenceJson> getMembers() {
        return members;
    }

    public void setMembers(List<UserReferenceJson> members) {
        this.members = members;
    }

    public String getGroupCluster() {
        return groupCluster;
    }

    public void setGroupCluster(String groupCluster) {
        this.groupCluster = groupCluster;
    }
}
