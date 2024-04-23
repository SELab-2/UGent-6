import apiCall from "../../../../util/apiFetch";
import { ApiRoutes } from "../../../../@types/requests.d";
import { TFunction } from "i18next";
import axios, {AxiosError} from "axios";


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
        if (axios.isAxiosError(error)) { // Controleer of de fout een AxiosError is
            const axiosError = error as AxiosError<string>; // Cast de error naar AxiosError
            if (axiosError.response) { // Controleer of er een response is in de fout
                return {
                    success: false,
                    message: axiosError.response.data
                };
            } else {
                console.error('Failed to leave the course:', axiosError);
                return {
                    success: false,
                    message: "Network error occurred"
                };
            }
        } else {
            console.error('Failed to leave the course:', error);
            return {
                success: false,
                message: "Unknown error occurred"
            };
        }
    }
};
