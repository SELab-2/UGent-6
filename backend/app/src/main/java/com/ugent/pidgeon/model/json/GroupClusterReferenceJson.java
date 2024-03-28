package com.ugent.pidgeon.model.json;

public record GroupClusterReferenceJson (
        long clusterId,
        String name,
        int groupCount,

        String clusterUrl
) {
    public GroupClusterReferenceJson {
    }
}