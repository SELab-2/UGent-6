import { Button, Card, Segmented, Tooltip, Typography } from "antd"
import { useTranslation } from "react-i18next"
import CreateCourseModal from "./components/CreateCourseModal"
import { useEffect, useState } from "react"
import HorizontalCourseScroll from "./components/HorizontalCourseScroll"
import apiCall from "../../util/apiFetch"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import ProjectTable from "./components/ProjectTable"
import ProjectTimeline from "../../components/other/ProjectTimeline"
import { useLocalStorage } from "usehooks-ts"
import { CalendarOutlined, NodeIndexOutlined, OrderedListOutlined, UnorderedListOutlined } from "@ant-design/icons"
import ProjectCalander from "../../components/other/ProjectCalander"

export type ProjectsType = GET_Responses[ApiRoutes.COURSE_PROJECTS]

type ProjectView = "table" | "timeline" | "calendar"

const Home = () => {
  const { t } = useTranslation()
  const [projects, setProjects] = useState<ProjectsType | null>(null)
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
        <HorizontalCourseScroll
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
        )}

        {projectsViewMode === "timeline" && <ProjectTimeline projects={projects} />}

        {projectsViewMode === "calendar" && (
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
