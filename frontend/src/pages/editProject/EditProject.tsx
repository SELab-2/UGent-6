import React, { useContext, useEffect, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button, Form, Card } from "antd"
import { useTranslation } from "react-i18next"
import ProjectForm from "../../components/forms/ProjectForm"
import { EditFilled } from "@ant-design/icons"
import { FormProps } from "antd/lib"
import { ProjectFormData } from "../projectCreate/components/ProjectCreateService"
import useProject from "../../hooks/useProject"
import dayjs from "dayjs"
import { ApiRoutes, GET_Responses, POST_Requests, POST_Responses } from "../../@types/requests.d"
import { AppRoutes } from "../../@types/routes"
import { ProjectContext } from "../../router/ProjectRoutes"
import useApi from "../../hooks/useApi"
import { DockerFormData } from "../../components/common/saveDockerForm"

const EditProject: React.FC = () => {
  const [form] = Form.useForm<ProjectFormData & DockerFormData>()
  const { t } = useTranslation()
  const { courseId, projectId } = useParams()
  const [loading, setLoading] = useState(false)
  const API = useApi()
  const [error, setError] = useState<JSX.Element | null>(null) // Gebruik ProjectError type voor error state
  const navigate = useNavigate()
  const project = useProject()
  const { updateProject } = useContext(ProjectContext)
  const [initialDockerValues, setInitialDockerValues] = useState<POST_Requests[ApiRoutes.PROJECT_TESTS] | null>(null)

  const updateDockerForm = async () => {
    if (!projectId) return
    const response = await API.GET(ApiRoutes.PROJECT_TESTS, { pathValues: { id: projectId } })

    let formVals:POST_Requests[ApiRoutes.PROJECT_TESTS] ={
      structureTest: null,
      dockerTemplate: null,
      dockerScript: null,
      dockerImage: null,
    }
    if (response.success) {
      const tests = response.response.data
      formVals = {
        structureTest: tests.structureTest ?? "",
        dockerTemplate: tests.dockerTemplate ?? "",
        dockerScript: tests.dockerScript ?? "",
        dockerImage: tests.dockerImage ?? "",
      }
    }

    form.setFieldsValue(formVals)

    setInitialDockerValues(formVals)

  }

  useEffect(() => {
    if (!project) return

    updateDockerForm()
  }, [project])

  const handleCreation = async () => {
    const values: ProjectFormData & DockerFormData = form.getFieldsValue()
    console.log(values)

    if (!courseId || !projectId) return console.error("courseId or projectId is undefined")
    setLoading(true)

    const response = await API.PUT(
      ApiRoutes.PROJECT,
      {
        body: values,
        pathValues: { id: projectId },
      },
      "alert"
    )

    if (!response.success) {
      setError(response.alert || null)
      setLoading(false)
      return
    }
    const result = response.response.data
    updateProject(result)
    navigate(AppRoutes.PROJECT.replace(":projectId", result.projectId.toString()).replace(":courseId", courseId)) // Navigeer naar het nieuwe project
  }

  const onInvalid: FormProps<ProjectFormData>["onFinishFailed"] = (e) => {
    const errField = e.errorFields[0].name[0]
    if (errField === "groupClusterId") navigate("#groups")
    else if (errField === "structureTest") navigate("#structure")
    else if (errField === "dockerScript" || errField === "dockerImage" || errField === "dockerTemplate") navigate("#tests")
    else navigate("#general")
  }

  if (!project) return <></>
  return (
    <>
      <Form
        initialValues={{
          name: project.name,
          description: project.description,
          groupClusterId: project.clusterId,
          visible: project.visible,
          maxScore: project.maxScore,
          deadline: dayjs(project.deadline),
        }}
        form={form}
        onFinishFailed={onInvalid}
        onFinish={handleCreation}
        layout="vertical"
        requiredMark="optional"
      >
        <div style={{ width: "100%", display: "flex", justifyContent: "center" }}>
          <ProjectForm
            form={form}
            error={error}
            cardProps={{
              title: t("project.change.updateTitle", { name: project.name }),
              extra: (
                <Form.Item style={{ textAlign: "center", margin: 0 }}>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<EditFilled />}
                    loading={loading}
                  >
                    {t("project.change.update")}
                  </Button>
                </Form.Item>
              ),
            }}
          />
        </div>
      </Form>
    </>
  )
}

export default EditProject
