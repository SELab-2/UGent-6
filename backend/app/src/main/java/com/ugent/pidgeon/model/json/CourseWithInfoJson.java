package com.ugent.pidgeon.model.json;

import java.util.List;

public record CourseWithInfoJson (
        Long courseId,
        String name,
        String description,
        UserReferenceJson teacher,
        List<UserReferenceJson> assistants,
        String memberUrl,
        String joinUrl
) {}
