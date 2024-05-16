import { FormInstance } from "antd";
import { ApiRoutes, POST_Requests } from "../../@types/requests.d";
import { UseApiType } from "../../hooks/useApi";



export type DockerFormData =  POST_Requests[ApiRoutes.PROJECT_TESTS]


const saveDockerForm = async (form:FormInstance, initialDockerValues: DockerFormData | null, API: UseApiType, projectId:string) => {
  if(!form.isFieldsTouched(["dockerImage", 'dockerScript', 'dockerTemplate', 'structureTest'])) return null

  let data:DockerFormData = form.getFieldsValue(['dockerImage', 'dockerScript', 'dockerTemplate', 'structureTest'])

  if(!initialDockerValues) {
    // We do a POST request
    console.log("POST", data);
    return API.POST(ApiRoutes.PROJECT_TESTS, { body: data, pathValues: {id: projectId}})
  }

  if(data.dockerImage == null || data.dockerImage.length === 0 ) {
    // We do a delete
    console.log("DELETE", data);
    return API.DELETE(ApiRoutes.PROJECT_TESTS, { pathValues: {id: projectId} })
  }

  // We do a PUT
  console.log("PUT", data);
  return API.PUT(ApiRoutes.PROJECT_TESTS, { body: data, pathValues: {id: projectId}})
}

export default saveDockerForm