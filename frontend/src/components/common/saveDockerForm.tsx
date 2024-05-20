import { FormInstance, GetProp, UploadProps } from "antd"
import { ApiRoutes, POST_Requests } from "../../@types/requests.d"
import { UseApiType } from "../../hooks/useApi"
import apiCall from "../../util/apiFetch"
import { RcFile } from "antd/es/upload"

export type DockerFormData = POST_Requests[ApiRoutes.PROJECT_TESTS]
type FileType = RcFile//Parameters<GetProp<UploadProps, "beforeUpload">>[0]

const saveDockerForm = async (form: FormInstance, initialDockerValues: DockerFormData | null, API: UseApiType, projectId: string) => {
  if (!form.isFieldsTouched(["dockerImage", "dockerScript", "dockerTemplate", "structureTest"])) return null

  let data: DockerFormData = form.getFieldsValue(["dockerImage", "dockerScript", "dockerTemplate", "structureTest"])

  if (!initialDockerValues) {
    // We do a POST request
    console.log("POST", data)
    await API.POST(ApiRoutes.PROJECT_TESTS, { body: data, pathValues: { id: projectId } })
  } else if (data.dockerImage == null || data.dockerImage.length === 0) {
    // We do a delete
    console.log("DELETE", data)
    await API.DELETE(ApiRoutes.PROJECT_TESTS, { pathValues: { id: projectId } })
  } else {
    // We do a PUT
    console.log("PUT", data)
    await API.PUT(ApiRoutes.PROJECT_TESTS, { body: data, pathValues: { id: projectId }, headers: {} })
  }

  if (form.isFieldTouched("dockerTestDir")) {
    const val: FileType | undefined = form.getFieldValue("dockerTestDir")?.[0]?.originFileObj
    
    if (val === undefined) {
      // DELETE
      await API.DELETE(ApiRoutes.PROJECT_TESTS_UPLOAD, { pathValues: { id: projectId } }, "message")
    } else {
      const formData = new FormData()
      formData.append("file", val, val.name)
      try {
        await apiCall.put(ApiRoutes.PROJECT_TESTS_UPLOAD, formData, {id: projectId})
      } catch(err){
        console.error(err);
      }

      // await API.PUT(
      //   ApiRoutes.PROJECT_TESTS_UPLOAD,
      //   {
      //     body: formData,
      //     pathValues: { id: projectId },
      //     headers: {
      //       "Content-Type": "multipart/form-data",
      //     },
      //   },
      //   "message"
      // )
    }
    console.log(val)
  }
}

export default saveDockerForm
