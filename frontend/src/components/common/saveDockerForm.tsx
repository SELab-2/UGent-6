import { FormInstance, GetProp, UploadProps } from "antd";
import { ApiRoutes, POST_Requests } from "../../@types/requests.d";
import { UseApiType } from "../../hooks/useApi";



export type DockerFormData =  POST_Requests[ApiRoutes.PROJECT_TESTS]
type FileType = Parameters<GetProp<UploadProps, 'beforeUpload'>>[0];


const saveDockerForm = async (form:FormInstance, initialDockerValues: DockerFormData | null, API: UseApiType, projectId:string) => {


  if(form.isFieldTouched('dockerTestDir')){
    const val: FileType|undefined = form.getFieldValue('dockerTestDir')?.file
    if(val === undefined) {
      // DELETE 
      await API.DELETE(ApiRoutes.PROJECT_TESTS_UPLOAD, { pathValues: {id: projectId} },"message")
    } else {
      const formData = new FormData()
      formData.append('file', val)
      await API.PUT(ApiRoutes.PROJECT_TESTS_UPLOAD, { body: {file:formData}, pathValues: {id: projectId} },"message")
    }
    console.log(val);
  }

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