import apiCall from "../../../../util/apiFetch";
import { ApiRoutes } from "../../../../@types/requests.d";
import { TFunction } from "i18next";


export interface LeaveStatus {
    success: boolean;
    message: string;
}

export const leaveCourse = async (courseId: string, t: TFunction<"translation", undefined> | undefined): Promise<LeaveStatus> => {
    try {
        const response = await apiCall.delete(ApiRoutes.COURSE_LEAVE, undefined, { courseId: courseId });

        // Check if the response status is OK (200)
        if (response.status === 200) {
            return {
                success: true,
                message: t ? t("course.leaveSuccess") : "Left course successfully"
            };
        } else {
            // Extract error message from response data
            const errorMessage = response.data?.message || "Failed to leave the course"
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
            message: t ? t("course.leaveFail") : "Failed to leave the course"
        };
    }
};
