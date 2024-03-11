package com.ugent.pidgeon.controllers;

public final class ApiRoutes {
    public static final String USER_BASE_PATH = "/api/users";
    public static final String COURSE_BASE_PATH = "/api/courses";
    public static final String DEADLINE_BASE_PATH = "/api/deadlines";
    public static final String PROJECT_BASE_PATH = "/api/projects";
    public static final String TEST_BASE_PATH = "/api/tests";
    public static final String FILE_BASE_PATH = "/api/files";
    public static final String SUBMISSION_BASE_PATH = "/api/files";
    public static final String GROUP_BASE_PATH = "/api/groups";
    public static final String GROUP_MEMBER_BASE_PATH = GROUP_BASE_PATH + "/{groupid}/members";
    public static final String GROUP_FEEDBACK_PATH = PROJECT_BASE_PATH + "/{projectid}/groups/{groupid}/score";
    public static final String CLUSTER_BASE_PATH = "/api/clusters";

}
