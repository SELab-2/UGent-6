package com.ugent.pidgeon.model.json;

import java.sql.Timestamp;
import java.util.List;

public record GroupClusterUpdateJson(
        String name,
        int capacity,
        int groupCount
        ) {
    public GroupClusterUpdateJson {
    }
}

