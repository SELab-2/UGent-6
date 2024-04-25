package com.ugent.pidgeon.model.json;

import java.time.OffsetDateTime;
import java.util.List;

public record GroupClusterJson(
        long clusterId,
        String name,
        int capacity,
        int groupCount,
        OffsetDateTime createdAt,
        List<GroupJson>  groups,

        String courseUrl
) {
    public GroupClusterJson {
    }
}

