package com.ugent.pidgeon.model.json;

import java.util.Map;

public class ClusterFillJson {

  private Map<Long, Long[]> clusterGroupMembers;


  public ClusterFillJson(Map<Long, Long[]> clusterGroupMembers) {
    this.clusterGroupMembers = clusterGroupMembers;
  }

  public ClusterFillJson() {
  }


  public Map<Long, Long[]> getClusterGroupMembers() {
    return clusterGroupMembers;
  }

  public void setClusterGroupMembers(Map<Long, Long[]> clusterGroupMembers) {
    this.clusterGroupMembers = clusterGroupMembers;
  }

}
