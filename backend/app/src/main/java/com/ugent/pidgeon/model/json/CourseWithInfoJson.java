package com.ugent.pidgeon.model.json;

import java.time.OffsetDateTime;
import java.util.List;

public record CourseWithInfoJson (
        Long courseId,
        String name,
        String description,
        UserReferenceJson teacher,
        List<UserReferenceJson> assistants,
        String memberUrl,
        String joinUrl,
        String joinKey,
        OffsetDateTime archivedAt
) {}

