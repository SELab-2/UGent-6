import { FC } from "react"
import ProjectTable from "./ProjectTable"
import { Card } from "antd"

const ProjectCard: FC = () => {
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
        projects={[
          {
            projectId: 1,
            name: "Opdracht 1",
            deadline: "2024-05-01T00:00:00Z",
            description: "Maak een programma dat dingen doet ... aaaaaa".repeat(10),
            submission_url: "/api/projects/1/submission",
            tests_url: "/api/projects/1/tests",
            course: {
              name: "Computationele biologie",
              url: "/api/courses/1",
              courseId: 1
            }
          },
          {
            projectId: 2,
            name: "Opdracht 2",
            deadline: "2024-06-01T00:00:00Z",
            description: "Maak een programma dat ...",
            submission_url: "/api/projects/2/submission",
            tests_url: "/api/projects/2/tests",
            course: {
              name: "Computationele biologie",
              url: "/api/courses/1",
              courseId: 1
            }
          },
        ]}
      />
    </Card>
  )
}

export default ProjectCard
