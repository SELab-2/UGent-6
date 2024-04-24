
import React, { useState } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { Button, Form, Card } from "antd"
import { useTranslation } from "react-i18next"
import Error from "../error/Error"
import ProjectForm from "../../components/forms/ProjectForm"
import { EditFilled, PlusOutlined } from "@ant-design/icons"
import { FormProps } from "antd/lib"
import { ProjectError, ProjectFormData } from "../projectCreate/components/ProjectCreateService"
import useProject from "../../hooks/useProject"
import dayjs from 'dayjs';


const EditProject: React.FC = () => {
  const [form] = Form.useForm<ProjectFormData>()
  const { t } = useTranslation()
  const { courseId } = useParams<{ courseId: string }>()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<ProjectError | null>(null) // Gebruik ProjectError type voor error state
  const [activeTab, setActiveTab] = useState("general")
  const project = useProject()
  
  const handleCreation = async () => {
    const values: ProjectFormData = form.getFieldsValue()
    console.log(values)

    if (!courseId) return console.error("courseId is undefined")
    setLoading(true)

    try {

    } catch (error: any) {
      // Vang netwerkfouten op
    
    } finally {
      setLoading(false)
    }
  }

  const onInvalid: FormProps<ProjectFormData>["onFinishFailed"] = (e) => {
    const errField = e.errorFields[0].name[0]
    if (errField === "groupClusterId") setActiveTab("groups")
    else if (errField === "structure") setActiveTab("structure")
    else if (errField === "dockerScript" || errField === "dockerImage" || errField === "sjabloon") setActiveTab("tests")
    else setActiveTab("general")
  }


  if(!project) return <></>
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
            activeTab={activeTab}
            onTabChange={setActiveTab}
            form={form}
            cardProps={{
              title: t("project.change.title"),
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
