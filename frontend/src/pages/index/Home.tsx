import { Card, Segmented, Typography } from "antd"
import { useTranslation } from "react-i18next"
import CreateCourseModal from "./components/CreateCourseModal"
import { useEffect, useState } from "react"
import apiCall from "../../util/apiFetch"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import ProjectTable from "./components/ProjectTable"
import ProjectTimeline from "../../components/other/ProjectTimeline"
import { useLocalStorage } from "usehooks-ts"
import { CalendarOutlined, NodeIndexOutlined, OrderedListOutlined, UnorderedListOutlined } from "@ant-design/icons"
import ProjectCalander from "../../components/other/ProjectCalander"
import CourseSection from "./components/CourseSection"

export type ProjectsType = GET_Responses[ApiRoutes.COURSE_PROJECTS]

type ProjectView = "table" | "timeline" | "calendar"

const Home = () => {
  const { t } = useTranslation()
  const [projects, setProjects] = useLocalStorage<ProjectsType | null>("__projects_cache",null)
  const [open, setOpen] = useState(false)
  const [projectsViewMode, setProjectsViewMode] = useLocalStorage<ProjectView>("projects_view", "table")

  useEffect(() => {
    apiCall.get(ApiRoutes.PROJECTS).then((res) => {
      const projects: ProjectsType = [...res.data.adminProjects, ...res.data.enrolledProjects.map((p) => ({ ...p.project, status: p.status }))]
      console.log("=>", projects)
      setProjects(projects)
    })
  }, [])

  return (
    <div>
      <div>
        <CourseSection
          projects={projects}
          onOpenNew={() => setOpen(true)}
        />
      </div>
      <br />
      <br />
      <div style={{ position: "relative", padding: "0 2rem" }}>
        <Typography.Title level={3}>
          {t("home.yourProjects")}

          <Segmented
            onChange={(e) => setProjectsViewMode(e as ProjectView)}
            style={{ float: "right" }}
            value={projectsViewMode}
            options={[
              { label: t("home.table"), value: "table", icon: <UnorderedListOutlined /> },
              { label: "Timeline", value: "timeline", icon: <NodeIndexOutlined /> },
              { label: t("home.calendar"), value: "calendar", icon: <CalendarOutlined /> },
            ]}
          />
        </Typography.Title>

        {projectsViewMode === "table" && (
          <Card className="projectTable"
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
        )}

          {projectsViewMode === "timeline" && <div className="timeline"><ProjectTimeline  projects={projects} /> </div>}

        {projectsViewMode === "calendar" && (
          <Card className="calendar"
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
            <ProjectCalander projects={projects} />
          </Card>
        )}
      </div>
      <div></div>
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
