import React, { useState } from "react"
import { useParams, useNavigate, useLocation } from "react-router-dom"
import { Button, Form, Card } from "antd"
import { useTranslation } from "react-i18next"
import { ProjectFormData, ProjectError } from "./components/ProjectCreateService"
import Error from "../error/Error"
import ProjectCreateService from "./components/ProjectCreateService"
import ProjectForm from "../../components/forms/ProjectForm"
import { AppRoutes } from "../../@types/routes"
import useAppApi from "../../hooks/useAppApi"
import { PlusOutlined } from "@ant-design/icons"
import { FormProps } from "antd/lib"

const ProjectCreate: React.FC = () => {
  const [form] = Form.useForm<ProjectFormData>()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { courseId } = useParams<{ courseId: string }>()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<ProjectError | null>(null) // Gebruik ProjectError type voor error state
  const location = useLocation()


  const { message } = useAppApi()

  const handleCreation = async () => {
    const values: ProjectFormData = form.getFieldsValue()
    console.log(values)

    if (!courseId) return console.error("courseId is undefined")
    setLoading(true)

    try {
      // Roep createProject aan en controleer op fouten
      const result = await ProjectCreateService.createProject(courseId, values)
      if (result.code === 200) {
        message.success(t("project.change.success")) // Toon een succesbericht
        navigate(AppRoutes.PROJECT.replace(":projectId", result.project!.projectId.toString()).replace(":courseId", courseId)) // Navigeer naar het nieuwe project
      } else setError(result) // Sla de fout op in de state
    } catch (error: any) {
      // Vang netwerkfouten op
      setError({
        code: 500, // Interne serverfoutcode
        message: error.message || "Unknown error occurred",
        project: null,
      })
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
      >
        <div style={{ width: "100%", display: "flex", justifyContent: "center" }}>
          <ProjectForm

            form={form}
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
