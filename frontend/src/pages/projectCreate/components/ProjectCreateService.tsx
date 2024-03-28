import {Timestamp} from "../../../@types/requests";

export interface ProjectFormData{
    name: string;
    description: string;
    groupClusterId: number;
    testId: number | null;
    visible: boolean;
    maxScore: number;
    deadline: Date | null;
}

interface ProjectError {
    code: number;
    message: string;
}

class ProjectCreateService {
    static async createProject(formData: ProjectFormData): Promise<boolean> {
        return true;
    }
}

export default ProjectCreateService;