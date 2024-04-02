import { FC, memo, useEffect, useState } from "react"
import ProjectTable, { ProjectType } from "./ProjectTable"
import { Card } from "antd"

const ProjectCard: FC<{courseId?:number}> = ({courseId}) => {
  const [projects,setProjects] = useState<ProjectType[]|null>(null)


  useEffect(()=> {
    // TODO:  do projects fetch to either all projects or course projects if courseId != undefined
    if(!courseId) return
    setTimeout(() => {
      setProjects([{
        projectId: 1,
        name: "Opdracht 1",
        deadline: "2024-05-01T00:00:00Z",
        description: "Maak een programma dat dingen doet ... aaaaaa".repeat(10),
        submission_url: "/api/projects/1/submission",
        tests_url: "/api/projects/1/tests",
        maxScore: 100,
        progress:{
          usersCompleted: 40,
          userCount: 61
        },
        state: "completed",
        course: {
          name: "Computationele biologie",
          url: "/api/courses/1",
          courseId: courseId
        }
      },
      {
        projectId: 2,
        name: "Opdracht 2",
        deadline: "2024-06-01T00:00:00Z",
        description: "Maak een programma dat ...",
        submission_url: "/api/projects/2/submission",
        tests_url: "/api/projects/2/tests",
        maxScore: 100,
        progress:{
          usersCompleted: 5,
          userCount: 61
        },
        state: "notStarted",
        course: {
          name: "Computationele biologie",
          url: "/api/courses/1",
          courseId: courseId
        }
      }])

    }, 300)

  },[courseId])
  console.log("======>", courseId);

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
