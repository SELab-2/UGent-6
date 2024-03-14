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
            id: 1,
            name: "Opdracht 1",
            deadline: "2024-05-01T00:00:00Z",
            description: "Maak een programma dat ...",
            submission_url: "/api/projects/1/submission",
            tests_url: "/api/projects/1/tests",
            course: "Computationele biologie",
          },
          {
            id: 2,
            name: "Opdracht 2",
            deadline: "2024-06-01T00:00:00Z",
            description: "Maak een programma dat ...",
            submission_url: "/api/projects/2/submission",
            tests_url: "/api/projects/2/tests",
            course: "Computationele biologie",
          },
        ]}
      />
    </Card>
  )
}

export default ProjectCard
