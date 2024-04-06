package com.ugent.pidgeon.model;

import com.ugent.pidgeon.model.json.CourseReferenceJson;
import com.ugent.pidgeon.model.json.ProjectProgressJson;

import java.sql.Timestamp;

public record ProjectResponseJson(
        CourseReferenceJson course,
        Timestamp deadline,
        String description,
        Long projectId,
        String name,
        String submissionUrl, //url to -> submissions of group for student, latest submissions for all groupss for teacher
        String testUrl, //url to -> test of group for student, all tests for teacher
        Integer maxScore,
        boolean visible,
        ProjectProgressJson progress
) {}
