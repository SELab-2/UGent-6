package com.ugent.pidgeon.model.json;

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

}
