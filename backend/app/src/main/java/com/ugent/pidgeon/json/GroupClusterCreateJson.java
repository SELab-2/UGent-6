package com.ugent.pidgeon.json;

import java.time.OffsetDateTime;

public record GroupClusterCreateJson(
        String name,
        Integer capacity,
        Integer groupCount,
        OffsetDateTime lockGroupsAfter
        ) {
    public GroupClusterCreateJson {
    }
}

