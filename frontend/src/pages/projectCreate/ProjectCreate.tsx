import React, { useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button, Form, Card } from "antd"
import { useTranslation } from "react-i18next"
import { ProjectFormData, ProjectError } from "./components/ProjectCreateService"
import Error from "../error/Error"
import ProjectCreateService from "./components/ProjectCreateService"
import ProjectForm from "../../components/forms/ProjectForm"
import { AppRoutes } from "../../@types/routes"
import useAppApi from "../../hooks/useAppApi"

const ProjectCreate: React.FC = () => {
  const [form] = Form.useForm()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { courseId } = useParams<{ courseId: string }>()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<ProjectError | null>(null) // Gebruik ProjectError type voor error state
  const { message } = useAppApi()

  const handleCreation = async (values: ProjectFormData) => {
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

  return (
    <>
      {error && (
        <Error
          errorCode={error.code}
          errorMessage={error.message}
        />
      )}
      {/* Toon Error-pagina als er een fout is */}

      <div style={{ width: "100%", display: "flex", justifyContent: "center" }}>
        <Card
          title={t("project.change.title")}
          style={{ maxWidth: "700px", width: "100%", margin: "2rem 0" }}
        >
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
            onFinish={handleCreation}
            layout="vertical"
            requiredMark="optional"
          >
            <ProjectForm />

            <Form.Item style={{textAlign:"center"}}>
              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
              >
                {t("project.change.create")}
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </div>
    </>
  )
}

export default ProjectCreate
