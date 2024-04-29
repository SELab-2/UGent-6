import React, { useContext, useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button, Form, Card } from "antd"
import { useTranslation } from "react-i18next"
import Error from "../error/Error"
import ProjectForm from "../../components/forms/ProjectForm"
import { EditFilled, PlusOutlined } from "@ant-design/icons"
import { FormProps } from "antd/lib"
import { ProjectError, ProjectFormData } from "../projectCreate/components/ProjectCreateService"
import useProject from "../../hooks/useProject"
import dayjs from "dayjs"
import apiCall from "../../util/apiFetch"
import { ApiRoutes } from "../../@types/requests.d"
import { AppRoutes } from "../../@types/routes"
import { ProjectContext } from "../../router/ProjectRoutes"

const EditProject: React.FC = () => {
  const [form] = Form.useForm<ProjectFormData>()
  const { t } = useTranslation()
  const { courseId,projectId } = useParams()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<ProjectError | null>(null) // Gebruik ProjectError type voor error state
  const navigate = useNavigate()
  const project = useProject()
  const { updateProject } = useContext(ProjectContext)

  const handleCreation = async () => {
    const values: ProjectFormData = form.getFieldsValue()
    console.log(values)

    if (!courseId || !projectId) return console.error("courseId or projectId is undefined")
    setLoading(true)

    try {
      const result = await apiCall.put(ApiRoutes.PROJECT, values, { id: projectId })
      updateProject(result.data)
      navigate(AppRoutes.PROJECT.replace(":projectId", result.data.projectId.toString()).replace(":courseId", courseId)) // Navigeer naar het nieuwe project
    } catch (error: any) {
      console.log(error);
      // Vang netwerkfouten op
    } finally {
      setLoading(false)
    }
  }

  const onInvalid: FormProps<ProjectFormData>["onFinishFailed"] = (e) => {
    const errField = e.errorFields[0].name[0]
    if (errField === "groupClusterId") navigate("#groups")
    else if (errField === "structure") navigate("#structure")
    else if (errField === "dockerScript" || errField === "dockerImage" || errField === "sjabloon") navigate("#tests")
    else navigate("#general")
  }

  if (!project) return <></>
  return (
    <>
      {error && (
        <Error
          errorCode={error.code}
          errorMessage={error.message}
        />
      )}
      {/* Toon Error-pagina als er een fout is */}

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
