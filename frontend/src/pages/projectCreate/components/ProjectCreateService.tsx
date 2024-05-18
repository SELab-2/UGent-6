import apiCall from "../../../util/apiFetch";
import {ApiRoutes, GET_Responses, POST_Requests, PUT_Requests} from "../../../@types/requests.d";


export type ProjectFormData = POST_Requests[ApiRoutes.PROJECT_CREATE] & {
    groups: PUT_Requests[ApiRoutes.CLUSTER_FILL]
}



export interface ProjectError {
    code: number;
    message: string;
    project: GET_Responses[ApiRoutes.PROJECT] | null;
}

class ProjectCreateService {
    static async createProject(courseId: string, formData: ProjectFormData): Promise<ProjectError> {
        try {
            const response = await apiCall.post(ApiRoutes.PROJECT_CREATE, formData, {courseId: courseId!});
            console.log(response.data)
            if (!response.data || response.status !== 200) {
                console.log(response.data)
                // Handle error response
                return {
                    code: response.status,
                    message: response.statusText || "Something went wrong",
                    project: null
                };
            } 
            
            return {
                code: 200,
                message: "Project created successfully",
                project: response.data
            }
        } catch (error: any) {
            if (error.response) {
                // The request was made and the server responded with a status code
                // that falls out of the range of 2xx
                console.error("Response error:", error.response.data);
                return {
                    code: error.response.status,
                    message: error.response.data || "Something went wrong",
                    project: null
                };
            } else if (error.request) {
                // The request was made but no response was received
                console.error("No response received:", error.request);
                return {
                    code: 500,
                    message: "No response received from the server",
                    project: null
                };
            } else {
                // Something happened in setting up the request that triggered an error
                console.error("Request error:", error.message);
                return {
                    code: 500,
                    message: "Error setting up the request",
                    project: null
                };
            }
        }
        
    }
}

export default ProjectCreateService;