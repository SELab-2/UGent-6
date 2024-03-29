import React, {createContext, FC, PropsWithChildren, useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import {useIsAuthenticated} from "@azure/msal-react";
import {UserCourseType} from "./UserProvider";
import apiCall from "../util/apiFetch";
import {ApiRoutes} from "../@types/requests";
import {CourseType} from "../pages/course/Course";

type CourseContextProps = {
    course: UserCourseType | null;
    fetchCourse: (courseKey: string) => void;
};

const CourseContext = createContext<CourseContextProps>({} as CourseContextProps);

const CourseProvider: FC<PropsWithChildren<{}>> = ({ children }) => {
    const { courseKey } = useParams<{ courseKey: string }>(); // Assuming you have courseKey as a route parameter
    const isAuthenticated = useIsAuthenticated();
    const [course, setCourse] = useState<CourseType | null>(null);

    useEffect(() => {
        if (isAuthenticated) {
            fetchCourse(courseKey);
        }
    }, [isAuthenticated, courseKey]);

    const fetchCourse = (key: string | undefined) => {
        apiCall
            .get(ApiRoutes.COURSE_FIND_WITH_KEY, { "key": key })
            .then((data) => {
                return setCourse(course);
            })
            .catch((error) => {
                console.error(error);
                // TODO: handle error
            });
    };

    return  <div>
            HELLO WORLD {course.c}
            </div>
};

export { CourseProvider, CourseContext };
