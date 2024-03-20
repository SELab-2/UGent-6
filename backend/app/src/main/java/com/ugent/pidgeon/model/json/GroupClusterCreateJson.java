package com.ugent.pidgeon.model.json;

public record GroupClusterCreateJson(
        String name,
        int capacity,
        int groupCount
        ) {
    public GroupClusterCreateJson {
    }
}

