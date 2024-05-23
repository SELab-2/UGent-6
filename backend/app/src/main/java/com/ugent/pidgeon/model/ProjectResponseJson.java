package com.ugent.pidgeon.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ugent.pidgeon.json.CourseReferenceJson;
import com.ugent.pidgeon.json.ProjectProgressJson;
import com.ugent.pidgeon.postgre.models.OffsetDateTimeSerializer;
import java.time.OffsetDateTime;

public record ProjectResponseJson(
        CourseReferenceJson course,

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime deadline,
        String description,
        Long projectId,
        String name,
        String submissionUrl, //url to -> submissions of group for student, latest submissions for all groupss for teacher
        String testUrl, //url to -> test of group for student, all tests for teacher
        Integer maxScore,
        boolean visible,
        ProjectProgressJson progress,
        Long groupId,
        Long clusterId,
        OffsetDateTime visibleAfter
) {}
