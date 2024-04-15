import { FC, useEffect, useState } from "react"
import ProjectTable, { ProjectType } from "./ProjectTable"
import {Button, Card} from "antd"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"
import useIsTeacher from "../../../hooks/useIsTeacher"
import {useTranslation} from "react-i18next";
import {AppRoutes} from "../../../@types/routes";
import {Link} from "react-router-dom";

const ProjectCard: FC<{ courseId?: number }> = ({ courseId }) => {
  const [projects, setProjects] = useState<ProjectType[] | null>(null)
  const isTeacher = useIsTeacher()
  const {t} = useTranslation()


  useEffect(() => {

    if (courseId) {
      apiCall.get(ApiRoutes.COURSE_PROJECTS, { id: courseId }).then((res) => {
        setProjects(res.data)
      })
    }
  }, [courseId])

  return (
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
      {isTeacher && (
          <div style={{marginTop: "1rem", textAlign: "right", paddingRight: "20px", paddingBottom: "10px"}}>
            <Button type="primary">
              <Link to={AppRoutes.PROJECT_CREATE.replace(":courseId", String(courseId))}>
                {t("project.newProject")}
              </Link>
            </Button>
          </div>
      )}
    </Card>
  )
}

export default ProjectCard
