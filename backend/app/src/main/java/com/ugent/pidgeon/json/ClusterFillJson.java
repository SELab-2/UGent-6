package com.ugent.pidgeon.json;

import java.util.HashMap;
import java.util.Map;

public class ClusterFillJson {
  private final Map<String, Long[]> clusterGroupMembers;

  public ClusterFillJson() {
    this.clusterGroupMembers = new HashMap<>();
  }

  public ClusterFillJson(Map<String, Long[]> clusterGroupMembers) {
    this.clusterGroupMembers = clusterGroupMembers;
  }

  public Map<String, Long[]> getClusterGroupMembers() {
    return clusterGroupMembers;
  }

  public void addClusterGroupMembers(String clusterId, Long[] groupIds) {
    clusterGroupMembers.put(clusterId, groupIds);
  }

}
