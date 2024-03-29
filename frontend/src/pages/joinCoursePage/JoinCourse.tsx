import React, { useEffect, useState } from "react";
import {Params, useParams} from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useIsAuthenticated } from "@azure/msal-react";=
import {ApiRoutes, GET_Responses} from "../../@types/requests";
import { UserCourseType } from "../../providers/UserProvider";
import apiCall from "../../util/apiFetch";
import {CourseType} from "../course/Course";

export type SimpleCourseType = GET_Responses[ApiRoutes.COURSE_FIND_WITH_KEY]

const JoinCourse = () => {
    const { courseId } = useParams<Params>();
// Assuming you have courseId as a route parameter
    const isAuthenticated = useIsAuthenticated();
    const { t } = useTranslation();
    const [course, setCourse] = useState<SimpleCourseType | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCourse = async () => {
            try {
                const response = await apiCall.get_param(ApiRoutes.COURSE_FIND_WITH_KEY, {courseId: courseId }); // Adjust this according to your API endpoint
                setCourse(response.data);
                setLoading(false);
            } catch (error) {
                setLoading(false);
            }
        };

        if (isAuthenticated) {
            fetchCourse();
        }
    }, [isAuthenticated, courseId]);

    if (loading) {
        return <div>{t("Loading...")}</div>;
    }

    if (error) {
        return <div>{t("Error: {{error}}", { error })}</div>;
    }

    if (!course) {
        return <div>{t("Course not found")}</div>;
    }

    // Render course information here
    return (
        <div>
            <h2>{course.name}</h2>
            <p>{course.description}</p>
            {/* Add more course details to render */}
        </div>
    );
};

export default JoinCourse;
