package com.ugent.pidgeon.json;

public record GroupClusterReferenceJson (
        long clusterId,
        String name,
        int groupCount,

        String clusterUrl
) {
    public GroupClusterReferenceJson {
    }
}