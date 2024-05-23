import { FormInstance} from "antd"
import { ApiRoutes, POST_Requests } from "../../@types/requests.d"
import { UseApiType } from "../../hooks/useApi"
import { RcFile } from "antd/es/upload"

export type DockerFormData = POST_Requests[ApiRoutes.PROJECT_TESTS]
type FileType = RcFile //Parameters<GetProp<UploadProps, "beforeUpload">>[0]

const saveDockerForm = async (form: FormInstance, initialDockerValues: (DockerFormData & {dockerMode:boolean}) | null, API: UseApiType, projectId: string) => {
  const dockerImage = form.getFieldValue("dockerImage")
  const dockerScript = form.getFieldValue("dockerScript")
  const dockerTemplate = form.getFieldValue("dockerTemplate")
  const structureTest = form.getFieldValue("structureTest")
  const dockerMode = form.getFieldValue("dockerMode")

  let success = true

  if (form.isFieldsTouched(["dockerImage", "dockerScript", "dockerTemplate", "structureTest", "dockerMode"]) && (!initialDockerValues || initialDockerValues.dockerImage !== dockerImage || initialDockerValues.dockerScript !== dockerScript || initialDockerValues.dockerTemplate !== dockerTemplate || initialDockerValues.structureTest !== structureTest || initialDockerValues.dockerMode !== dockerMode)) {
    let data: DockerFormData = form.getFieldsValue(["dockerImage", "dockerScript", "dockerTemplate", "structureTest"])
    if (!data.dockerImage?.length) {
      data.dockerScript = null
      data.dockerTemplate = null
    }
    if (!dockerMode) data.dockerTemplate = null

    if (!initialDockerValues) {
      // We do a POST request
      const r = await API.POST(ApiRoutes.PROJECT_TESTS, { body: data, pathValues: { id: projectId } }, "message")
      success &&= r.success
    } else if ((data.dockerImage == null || data.dockerImage.length === 0) && !data.structureTest?.length) {
      // We do a delete
      const r = await API.DELETE(ApiRoutes.PROJECT_TESTS, { pathValues: { id: projectId } }, "message")
      success &&= r.success
    } else {
      // We do a PUT
      const r = await API.PUT(ApiRoutes.PROJECT_TESTS, { body: data, pathValues: { id: projectId }, headers: {} }, "message")
      success &&= r.success
    }
  }

  if (form.isFieldTouched("dockerTestDir")) {
    const val: FileType | undefined = form.getFieldValue("dockerTestDir")?.[0]?.originFileObj

    if (val === undefined) {
      // DELETE
      const r = await API.DELETE(ApiRoutes.PROJECT_TESTS_UPLOAD, { pathValues: { id: projectId } }, "message")
      success &&= r.success
    } else {
      const formData = new FormData()
      formData.append("file", val, val.name)
      const r = await API.PUT(ApiRoutes.PROJECT_TESTS_UPLOAD, { body: formData, pathValues: { id: projectId } }, "message")
      success &&= r.success

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
  }
  return success
}

export default saveDockerForm
