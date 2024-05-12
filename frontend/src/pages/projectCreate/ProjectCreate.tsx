import React, { useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button, Form } from "antd"
import { useTranslation } from "react-i18next"
import { ProjectFormData } from "./components/ProjectCreateService"
import Error from "../error/Error"
import ProjectForm from "../../components/forms/ProjectForm"
import { AppRoutes } from "../../@types/routes"
import useAppApi from "../../hooks/useAppApi"
import { PlusOutlined } from "@ant-design/icons"
import { FormProps } from "antd/lib"
import saveDockerForm, { DockerFormData } from "../../components/common/saveDockerForm"
import useApi from "../../hooks/useApi"
import { ApiRoutes } from "../../@types/requests.d"

const ProjectCreate: React.FC = () => {
  const [form] = Form.useForm<ProjectFormData & DockerFormData>()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { courseId } = useParams<{ courseId: string }>()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<JSX.Element | null>(null) // Gebruik ProjectError type voor error state
  const API = useApi()
  const { message } = useAppApi()

  const handleCreation = async () => {
    const values: ProjectFormData & DockerFormData = form.getFieldsValue()
    const project: ProjectFormData = {
      name: values.name,
      description: values.description,
      groupClusterId: values.groupClusterId,
      deadline: values.deadline,
      maxScore: values.maxScore,
      testId: values.testId,
      visible: values.visible,
    }

    if (!courseId) return console.error("courseId is undefined")
    setLoading(true)

    const response = await API.POST(ApiRoutes.PROJECT_CREATE, { body: project, pathValues: { courseId } }, "alert")
    if (!response.success) {
      setError(response.alert || null)
      return setLoading(false)
    }
    const result = response.response.data

    await saveDockerForm(
      form,
      {
        dockerImage: null,
        dockerScript: null,
        dockerTemplate: null,
        structureTest: null,
      },
      API
    )

    message.success(t("project.change.success")) // Toon een succesbericht
    navigate(AppRoutes.PROJECT.replace(":projectId", result.projectId.toString()).replace(":courseId", courseId)) // Navigeer naar het nieuwe project

  }

  const onInvalid: FormProps<ProjectFormData>["onFinishFailed"] = (e) => {
    const errField = e.errorFields[0].name[0]
    if (errField === "groupClusterId") navigate("#groups")
    else if (errField === "structureTest") navigate("#structure")
    else if (errField === "dockerScript" || errField === "dockerImage" || errField === "dockerTemplate") navigate("#tests")
    else navigate("#general")
  }

  return (
    <>

      <Form
        initialValues={{
          name: "",
          description: "",
          groupClusterId: undefined,
          visible: false, // Stel de standaardwaarde in op false
          maxScore: 20,
          deadline: null,
        }}
        form={form}
        onFinishFailed={onInvalid}
        onFinish={handleCreation}
        layout="vertical"
        requiredMark="optional"
        validateTrigger="onBlur"
      >
        <div style={{ width: "100%", display: "flex", justifyContent: "center" }}>
          <ProjectForm
            form={form}
            error={error}
            cardProps={{
              title: t("project.change.title"),
              extra: (
                <Form.Item style={{ textAlign: "center", margin: 0 }}>
                  <Button
                    type="primary"
                    htmlType="submit"
                    icon={<PlusOutlined />}
                    loading={loading}
                  >
                    {t("project.change.create")}
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

export default ProjectCreate
