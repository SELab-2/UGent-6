package com.ugent.pidgeon.controllers;

public final class ApiRoutes {
    public static final String USER_BASE_PATH = "/api/users";
    public static final String COURSE_BASE_PATH = "/api/courses";

    public static final String PROJECT_BASE_PATH = "/api/projects";

    public static final String GROUP_BASE_PATH = "/api/groups/{groupid}";
    public static final String GROUP_MEMBER_BASE_PATH = GROUP_BASE_PATH + "/members";
    public static final String GROUP_FEEDBACK_PATH = PROJECT_BASE_PATH + "/{projectid}/groups/{groupid}/score";
}
