package com.ugent.pidgeon.model.json;

import java.sql.Timestamp;
import java.util.List;

public record GroupClusterJson(
        long clusterId,
        String name,
        int capacity,
        int groupCount,
        Timestamp createdAt,
        List<GroupReferenceJson>  groups) {
    public GroupClusterJson {
    }
}

