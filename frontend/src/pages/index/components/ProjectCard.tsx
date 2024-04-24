import { FC, useEffect, useState } from "react"
import ProjectTable, { ProjectType } from "./ProjectTable"
import { Button, Card } from "antd"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"
import useIsTeacher from "../../../hooks/useIsTeacher"
import { useTranslation } from "react-i18next"
import { AppRoutes } from "../../../@types/routes"
import { Link, useNavigate } from "react-router-dom"
import CourseAdminView from "../../../hooks/CourseAdminView"
import { PlusOutlined } from "@ant-design/icons"

const ProjectCard: FC<{ courseId?: number }> = ({ courseId }) => {
  const [projects, setProjects] = useState<ProjectType[] | null>(null)
  const { t } = useTranslation()
  const navigate = useNavigate()

  useEffect(() => {
    if (courseId) {
      apiCall.get(ApiRoutes.COURSE_PROJECTS, { id: courseId }).then((res) => {
        setProjects(res.data)
      })
    }
  }, [courseId])

  return (
    <>
      <CourseAdminView>
        <div style={{ textAlign: "right", paddingRight: "20px", paddingBottom: "10px" }}>
          <Button
            onClick={() => navigate(AppRoutes.PROJECT_CREATE.replace(":courseId", String(courseId)))}
            icon={<PlusOutlined />}
            type="primary"
          >
            {t("project.newProject")}
          </Button>
        </div>
      </CourseAdminView>
      <Card
        style={{
          width: "100%",
          overflow: "auto",
        }}
        styles={{
          body: {
            padding: "0",
          },
        }}
      >
        <ProjectTable
          ignoreColumns={courseId == undefined ? ["course"] : []}
          projects={projects}
        />
      </Card>
    </>
  )
}

export default ProjectCard
