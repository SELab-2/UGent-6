import apiCall from "../../../util/apiFetch";
import {ApiRoutes, Timestamp} from "../../../@types/requests.d";


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

            if (!response.data || response.status !== 200) {
                // Handle error response
                const errorData = response.data || {};
                return {
                    code: response.status,
                    message: errorData.id || "Something went wrong"
                };
            }
        } catch (error: any) {
            return {
                code: 500,
                message: "Something went wrong"
            };
        }
    }
}

export default ProjectCreateService;