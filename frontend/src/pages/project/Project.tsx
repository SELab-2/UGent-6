import { Button, Card, Tabs, TabsProps, Tooltip, theme } from "antd"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { Link, useLocation, useNavigate, useParams } from "react-router-dom"
import SubmissionCard from "./components/SubmissionTab"
import useCourse from "../../hooks/useCourse"
import useProject from "../../hooks/useProject"
import ScoreCard from "./components/ScoreTab"
import CourseAdminView from "../../hooks/CourseAdminView"
import {
  DeleteOutlined,
  DownloadOutlined,
  HeatMapOutlined,
  InfoOutlined,
  PlusOutlined,
  SendOutlined,
  SettingFilled,
  TeamOutlined
} from "@ant-design/icons"
import { useMemo, useState } from "react"
import useIsCourseAdmin from "../../hooks/useIsCourseAdmin"
import GroupTab from "./components/GroupTab"
import { AppRoutes } from "../../@types/routes"
import SubmissionsTab from "./components/SubmissionsTab"
import MarkdownTextfield from "../../components/input/MarkdownTextfield"
import apiCall from "../../util/apiFetch"

//  dracula, darcula,oneDark,vscDarkPlus  | prism, base16AteliersulphurpoolLight, oneLight

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const Project = () => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const course = useCourse()
  const { projectId } = useParams()
  const project = useProject()
  const courseAdmin = useIsCourseAdmin()
  const navigate = useNavigate()
  const location = useLocation()
  const [activeTab, setActiveTab] = useState(location.hash.slice(1) || "description")

  const now = Date.now()
  const deadline = new Date(project?.deadline ?? "").getTime()

  const items: TabsProps["items"] = useMemo(() => {
    const items: TabsProps["items"] = [
      {
        key: "description",
        label: t("home.projects.description"),
        icon: <InfoOutlined />,
        children: project && (
          <div style={{ padding: "0 8rem" }}>
            <MarkdownTextfield content={project.description} />
          </div>
        ),
      },
      {
        key: "groups",
        label: t("course.groups"),
        icon: <TeamOutlined />,
        children: <GroupTab />,
      },
      {
        key: "submissions",
        label: t("project.submissions"),
        icon: <SendOutlined />,
        children: courseAdmin ? (
          <span>
            <SubmissionsTab />
          </span>
        ) : (
          <SubmissionCard
            projectId={Number(projectId)}
            courseId={course.courseId}
          />
        ),
      },
    ]

    if (!courseAdmin) {
      items.push({
        key: "score",
        label: t("course.score"),
        children: <ScoreCard />,
      })
    }

    return items
  }, [project, course, courseAdmin])

  const changeTab = (key: string) => {
    navigate(`#${key}`)
    setActiveTab(key)
  }

  const handleNewSubmission = () => {
    navigate(AppRoutes.NEW_SUBMISSION.replace(AppRoutes.PROJECT + "/", ""))
  }

  const deleteProject = async () => {
    if(!project || !course) return console.error("project is undefined")
    await apiCall.delete(ApiRoutes.PROJECT, undefined, { id: project!.projectId+"" })

    navigate(AppRoutes.COURSE.replace(":courseId", course.courseId+""))
  }

  return (
    <div style={{ margin: "3rem 0", width: "100%", paddingBottom: "3rem" }}>
      <Card
        styles={{
          header: {
            background: token.colorPrimaryBg,
          },
          title: {
            fontSize: "1.1em",
          },
          body: {
            textWrap: "wrap",
            padding: "0.5rem",
          },
        }}
        style={{ width: "100%", marginBottom: "3rem" }}
        title={project?.name}
        loading={!project}
        extra={
          courseAdmin ? (<>
            <Link to="tests">
              <Button
                type="primary"
                icon={<HeatMapOutlined />}
                style={{marginLeft:"1rem"}}
              >
                {t("project.tests.toTests")}
              </Button>
            </Link>
            <Link to="edit">
              <Button
                type="primary"
                icon={<SettingFilled />}
                style={{marginLeft:"1rem"}}
              >
                {t("project.options")}
              </Button>
            </Link>
            <Button style={{marginLeft:"1rem"}} type="primary" onClick={deleteProject} danger icon={<DeleteOutlined/>} />
            </>
          ) : (
            <Tooltip title={now > deadline ? t("project.deadlinePassed") : ""}>
              <span>
                <Button
                  disabled={now < deadline}
                  type="primary"
                  onClick={handleNewSubmission}
                  icon={<PlusOutlined />}
                >
                  {t("project.newSubmission")}
                </Button>
              </span>
            </Tooltip>
          )
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={changeTab}
          items={items}
          tabBarExtraContent={
            activeTab === "submissions" ? (
              <CourseAdminView>
                <Button icon={<DownloadOutlined />}>{t("project.downloadSubmissions")}</Button>
              </CourseAdminView>
            ) : null
          }
        />
      </Card>
    </div>
  )
}
export default Project
