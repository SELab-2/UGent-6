import { FC, useEffect, useState } from "react"
import ProjectTable, { ProjectType } from "./ProjectTable"
import { Card } from "antd"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"

const ProjectCard: FC<{courseId?:number}> = ({courseId}) => {
  const [projects,setProjects] = useState<ProjectType[]|null>(null)


  useEffect(()=> {
    // TODO:  do projects fetch to either all projects or course projects if courseId != undefined


    if(!courseId) {
      apiCall.get(ApiRoutes.PROJECTS).then((res) => {
        //setProjects(res.data)

          setProjects([{
            projectId: 1,
            name: "Opdracht 1",
            deadline: "2024-05-01T00:00:00Z",
            description: "Maak een programma dat dingen doet ... aaaaaa".repeat(10),
            submissionUrl: "/api/projects/1/submission",
            testsUrl: "/api/projects/1/tests",
            maxScore: 100,
            progress:{
              completed: 40,
              total: 61
            },
            course: {
              name: "Computationele biologie",
              url: "/api/courses/1",
              courseId: courseId??1
            },
            visible: true
          },
          {
            projectId: 2,
            name: "Opdracht 2",
            deadline: "2024-06-01T00:00:00Z",
            description: "Maak een programma dat ...",
            submissionUrl: "/api/projects/2/submission",
            testsUrl: "/api/projects/2/tests",
            maxScore: 100,
            progress:{
              completed: 5,
              total: 61
            },
            course: {
              name: "Computationele biologie",
              url: "/api/courses/1",
              courseId: courseId??1
            },
            visible: true
          }])
    
      })
    } else {
      apiCall.get(ApiRoutes.COURSE_PROJECTS,{id:courseId}).then((res) => {
        setProjects(res.data)
      })
    }
    
     

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
