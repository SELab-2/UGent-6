import { FC, memo, useEffect, useState } from "react"
import ProjectTable, { ProjectType } from "./ProjectTable"
import { Card } from "antd"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"

const ProjectCard: FC<{courseId?:number}> = ({courseId}) => {
  const [projects,setProjects] = useState<ProjectType[]|null>(null)


  useEffect(()=> {
    // TODO:  do projects fetch to either all projects or course projects if courseId != undefined


    apiCall.get(ApiRoutes.COURSE_PROJECTS)
  
     

  },[courseId])

  return (
    <Card
      style={{
        width: "100%",
        overflow: "auto",
      }}
      styles={{
        body: {
          padding:"0"
        }
      }}
    >
      <ProjectTable
        ignoreColumns={courseId == undefined?  ["course"]:[]}
        projects={projects}
      />
    </Card>
  )
}

export default ProjectCard
