import apiCall from "../../../util/apiFetch";
import {ApiRoutes} from "../../../@types/requests.d";


export interface ProjectFormData {
    name: string;
    description: string;
    groupClusterId: number;
    testId: number | null;
    visible: boolean;
    maxScore: number;
    deadline: Date | null;
}

export interface ProjectError {
    code: number;
    message: string;
}

class ProjectCreateService {
    static async createProject(courseId: string, formData: ProjectFormData): Promise<ProjectError | void> {
        try {
            const response = await apiCall.post(ApiRoutes.PROJECT_CREATE, formData, {courseId: courseId!});
            console.log(response.data)
            if (!response.data || response.status !== 200) {
                console.log(response.data)
                // Handle error response
                const errorData = response.data || {};
                return {
                    code: response.status,
                    message: response.statusText || "Something went wrong"
                };
            }
        } catch (error: any) {
            if (error.response) {
                // The request was made and the server responded with a status code
                // that falls out of the range of 2xx
                console.error("Response error:", error.response.data);
                return {
                    code: error.response.status,
                    message: error.response.data || "Something went wrong"
                };
            } else if (error.request) {
                // The request was made but no response was received
                console.error("No response received:", error.request);
                return {
                    code: 500,
                    message: "No response received from the server"
                };
            } else {
                // Something happened in setting up the request that triggered an error
                console.error("Request error:", error.message);
                return {
                    code: 500,
                    message: "Error setting up the request"
                };
            }
        }
    }
}

export default ProjectCreateService;