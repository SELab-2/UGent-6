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
        return;
        // const route = `api/courses/${courseId}/create`
        // try {
        //     const response = await fetch(route, {
        //         method: 'POST',
        //         headers: {
        //             'Content-Type': 'application/json'
        //         },
        //         body: JSON.stringify(formData)
        //     });
        //
        //     if (!response.ok) {
        //         // Handle error response
        //         const errorData = await response.json(); // Assuming the error response contains JSON data
        //         return {
        //             code: response.status,
        //             message: errorData.message || "Something went wrong" // Assuming there is a 'message' field in the error response
        //         };
        //     }
        // } catch (error: any) {
        //     return {
        //         code: 500,
        //         message: "Something went wrong"
        //     };
        // }
    }
}

export default ProjectCreateService;