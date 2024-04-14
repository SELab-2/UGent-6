import { Button, Card, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import CourseCard from "./components/CourseCard"
import { PlusOutlined } from "@ant-design/icons"
import CreateCourseModal from "./components/CreateCourseModal"
import { useEffect, useState } from "react"
import ProjectCard from "./components/ProjectCard"
import useUser from "../../hooks/useUser"
import HorizontalCourseScroll from "./components/HorizontalCourseScroll"
import apiCall from "../../util/apiFetch"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import ProjectTable from "./components/ProjectTable"

export type ProjectsType = GET_Responses[ApiRoutes.COURSE_PROJECTS]

const Home = () => {
  const { t } = useTranslation()
  const [projects, setProjects] = useState<ProjectsType | null>(null)
  const [open, setOpen] = useState(false)

  useEffect(() => {
    apiCall.get(ApiRoutes.PROJECTS).then((res) => {
      const projects = res.data.adminProjects.concat(res.data.enrolledProjects)
      setProjects(projects)
    })
  }, [])

  return (
    <div>
      <div>
       

        <HorizontalCourseScroll projects={projects} onOpenNew={()=> setOpen(true)} />
      </div>
      <br />
      <br />
      <div style={{ position: "relative", padding: "0 2rem" }}>
        <Typography.Title level={3}>{t("home.yourProjects")}</Typography.Title>

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
          <ProjectTable projects={projects} />
        </Card>
      </div>
      <br />
      <br />

      <CreateCourseModal
        open={open}
        setOpen={setOpen}
      />
    </div>
  )
}

export default Home
