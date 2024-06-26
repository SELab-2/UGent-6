import { Card, Segmented, Typography } from "antd"
import { useTranslation } from "react-i18next"
import CreateCourseModal from "./components/CreateCourseModal"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import ProjectTable from "./components/ProjectTable"
import ProjectTimeline from "../../components/other/ProjectTimeline"
import { useLocalStorage } from "usehooks-ts"
import { CalendarOutlined, NodeIndexOutlined, OrderedListOutlined, UnorderedListOutlined } from "@ant-design/icons"
import ProjectCalander from "../../components/other/ProjectCalander"
import CourseSection from "./components/CourseSection"
import useApi from "../../hooks/useApi"
import createCourseModal from "./components/CreateCourseModal"

export type ProjectsType = GET_Responses[ApiRoutes.COURSE_PROJECTS]

type ProjectView = "table" | "timeline" | "calendar"

const Home = () => {
  const { t } = useTranslation()
  const [projects, setProjects] = useLocalStorage<ProjectsType | null>("__projects_cache",null)
  const [projectsViewMode, setProjectsViewMode] = useLocalStorage<ProjectView>("projects_view", "table")
  const API = useApi()
  const courseModal = createCourseModal()

  useEffect(() => {
    let ignore=  false

    API.GET(ApiRoutes.PROJECTS, {}).then((res) => {
      if(!res.success || ignore) return
      const projects: ProjectsType = [...res.response.data.adminProjects, ...res.response.data.enrolledProjects.map((p) => ({ ...p.project, status: p.status }))]
      
      projects.sort((a, b) => {
        const today = new Date();
        const date1 = new Date(a.deadline);
        const date2 = new Date(b.deadline);
    
        // Calculate the difference in time from today for each date
        const diff1 = date1.getTime() - today.getTime();
        const diff2 = date2.getTime() - today.getTime();
    
        // If both dates are in the future or both in the past, compare their absolute differences
        if ((diff1 >= 0 && diff2 >= 0) || (diff1 < 0 && diff2 < 0)) {
          const absDiff1 = Math.abs(diff1);
          const absDiff2 = Math.abs(diff2);
    
          if (absDiff1 < absDiff2) {
              return -1;
          } else if (absDiff1 > absDiff2) {
              return 1;
          } else {
              return 0;
          }
        }
    
        // If one date is in the future and the other is in the past, the future date has higher priority
        if (diff1 >= 0 && diff2 < 0) {
            return -1;
        } else if (diff1 < 0 && diff2 >= 0) {
            return 1;
        }
    
        // This should not be reached because all cases are covered
        return 0;
      })

      setProjects(projects)
    })

    return () => {
      ignore = true
    }
  }, [])

  return (
    <div>
      <div>
        <CourseSection
          projects={projects}
          onOpenNew={() => courseModal.showModal()}
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

     
    </div>
  )
}

export default Home
