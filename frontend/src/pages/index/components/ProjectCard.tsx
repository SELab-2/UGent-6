import { FC, useEffect, useState } from "react"
import ProjectTable, { ProjectType } from "./ProjectTable"
import { Card } from "antd"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"

const ProjectCard: FC<{ courseId?: string }> = ({ courseId }) => {
  const [projects, setProjects] = useState<ProjectType[] | null>(null)

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
    </Card>
  )
}

export default ProjectCard
