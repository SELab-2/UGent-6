package com.ugent.pidgeon.model.json;

public record GroupClusterCreateJson(
        String name,
        Integer capacity,
        Integer groupCount
        ) {
    public GroupClusterCreateJson {
    }
}

