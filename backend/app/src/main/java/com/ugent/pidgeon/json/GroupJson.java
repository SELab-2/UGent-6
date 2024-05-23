package com.ugent.pidgeon.json;

import java.util.List;

public class GroupJson {

    private Integer capacity;
    private Long groupId;
    private String name;
    private String groupClusterUrl;

    private List<UserReferenceJson> members;

    public GroupJson() {
    }

    public GroupJson(Integer capacity, Long groupId, String name, String groupClusterUrl) {
      this.capacity = capacity;
      this.groupId = groupId;
        this.name = name;
        this.groupClusterUrl = groupClusterUrl;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
