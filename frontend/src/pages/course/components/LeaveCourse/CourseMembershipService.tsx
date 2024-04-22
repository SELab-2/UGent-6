import apiCall from "../../../../util/apiFetch";
import {ApiRoutes} from "../../../../@types/requests.d";
import {useTranslation} from "react-i18next";


export interface LeaveStatus {
    success: boolean;
    message: string;
}


export const leaveCourse = async (courseId: string): Promise<LeaveStatus> => {
    const { t } = useTranslation()
    try {
        const response = await apiCall.delete(ApiRoutes.COURSE_LEAVE, undefined, { courseId: courseId });

        // Check if the response status is OK (200)
        if (response.status === 200) {
            return {
                success: true,
                message: t("course.leaveSuccess")
            };
        } else {
            // Extract error message from response data
            const errorMessage = response.data?.message || t("course.leaveFail");
            return {
                success: false,
                message: errorMessage
            };
        }
    } catch (error) {
        // Handle network errors or other exceptions
        console.error('Failed to leave the course:', error);
        return {
            success: false,
            message: t("course.leaveFail")
        };
    }
};
