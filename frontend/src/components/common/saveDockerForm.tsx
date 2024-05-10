import { FormInstance } from "antd";
import { ApiRoutes, POST_Requests } from "../../@types/requests";
import { UseApiType } from "../../hooks/useApi";



export type DockerFormData =  POST_Requests[ApiRoutes.PROJECT_TESTS]


const saveDockerForm = async (form:FormInstance, initialDockerValues: DockerFormData, apiHook: UseApiType) => {
  if(!form.isFieldsTouched(["dockerImage", 'dockerScript', 'dockerTemplate', 'structureTest'])) return null

  let data:DockerFormData = form.getFieldsValue(['dockerImage', 'dockerScript', 'dockerTemplate', 'structureTest'])
  console.log(data);

  if(!initialDockerValues.dockerImage) {
    // We do a POST request
    console.log("POST", data);
    return
  }

  if(data.dockerImage === null) {
    // We do a delete
    console.log("DELETE", data);
    return
  }

  // We do a PUT
  console.log("PUT", data);
}

export default saveDockerForm