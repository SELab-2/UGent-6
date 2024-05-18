import { Button, Card, Popconfirm, Tabs, TabsProps, Tooltip, theme } from "antd"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { Link, useLocation, useNavigate, useParams } from "react-router-dom"
import SubmissionCard from "./components/SubmissionTab"
import useCourse from "../../hooks/useCourse"
import useProject from "../../hooks/useProject"
import ScoreCard from "./components/ScoreTab"
import CourseAdminView from "../../hooks/CourseAdminView"
import { DeleteOutlined, DownloadOutlined, HeatMapOutlined, InfoCircleOutlined, PlusOutlined, SendOutlined, SettingFilled, TeamOutlined } from "@ant-design/icons"
import { useMemo, useState } from "react"
import useIsCourseAdmin from "../../hooks/useIsCourseAdmin"
import GroupTab from "./components/GroupTab"
import { AppRoutes } from "../../@types/routes"
import SubmissionsTab from "./components/SubmissionsTab"
import MarkdownTextfield from "../../components/input/MarkdownTextfield"
import useApi from "../../hooks/useApi"

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
  const API = useApi()

  const now = Date.now()
  const deadline = new Date(project?.deadline ?? "").getTime()

  const items: TabsProps["items"] = useMemo(() => {
    const items: TabsProps["items"] = [
      {
        key: "description",
        label: t("home.projects.description"),
        icon: <InfoCircleOutlined />,
        children: project && (
          <div style={{ display: "flex", justifyContent: "center", width: "100%" }}>
            <div style={{ maxWidth: "800px", width: "100%" }}>
              <MarkdownTextfield content={project.description} />
            </div>
          </div>
        ),
      },
    ]

    // if individual project -> do not show groups tab
    if (project?.clusterId) {
      items.push({
        key: "groups",
        label: t("course.groups"),
        icon: <TeamOutlined />,
        children: <GroupTab />,
      })
    }

    // if we work without groups -> always show submissions & score
    // if we work with groups -> only show submissions if we are in a group
    // if we are course admin -> always show submissions but not score 
    if((project?.groupId || !project?.clusterId) || courseAdmin) {

      items.push({
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
      })
    }
    
    if ((project?.groupId || !project?.clusterId) && !courseAdmin) {
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
    if (!project || !course) return console.error("project is undefined")
    const res = await API.DELETE(
      ApiRoutes.PROJECT,
      { pathValues: { id: project.projectId } },
      {
        mode: "message",
        successMessage: t("project.successfullyDeleted"),
      }
    )
    if (!res.success) return
    navigate(AppRoutes.COURSE.replace(":courseId", course.courseId + ""))
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
          courseAdmin ? (
            <>
              <Link to="edit">
                <Button
                  type="primary"
                  icon={<SettingFilled />}
                  style={{ marginLeft: "1rem" }}
                >
                  {t("project.options")}
                </Button>
              </Link>
              <Popconfirm
                title={t("project.deleteProject")}
                description={t("project.deleteProjectDescription")}
                onConfirm={deleteProject}
              >
                <Button
                  style={{ marginLeft: "1rem" }}
                  type="primary"
                  danger
                  icon={<DeleteOutlined />}
                />
              </Popconfirm>
            </>
          ) : (
            <Tooltip title={now > deadline ? t("project.deadlinePassed") : ""}>
              <span>
                <Button
                  disabled={now > deadline}
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
