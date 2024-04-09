import { Button, Card, Tabs, TabsProps, Tooltip, theme } from "antd"
import { ApiRoutes, GET_Responses } from "../../@types/requests"
import Markdown from "react-markdown"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { oneDark, oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"
import useApp from "../../hooks/useApp"
import { useTranslation } from "react-i18next"
import { Link, useLocation, useNavigate, useParams } from "react-router-dom"
import SubmissionCard from "./components/SubmissionTab"
import useCourse from "../../hooks/useCourse"
import useProject from "../../hooks/useProject"
import ScoreCard from "./components/ScoreTab"
import CourseAdminView from "../../hooks/CourseAdminView"
import { DownloadOutlined, PlusOutlined, SettingFilled } from "@ant-design/icons"
import { useMemo, useState } from "react"
import useIsCourseAdmin from "../../hooks/useIsCourseAdmin"
import GroupTab from "./components/GroupTab"
import { AppRoutes } from "../../@types/routes"
import SubmissionsTab from "./components/SubmissionsTab"

//  dracula, darcula,oneDark,vscDarkPlus  | prism, base16AteliersulphurpoolLight, oneLight

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const Project = () => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const app = useApp()
  const course = useCourse()
  const { projectId } = useParams()
  const project = useProject()
  const courseAdmin = useIsCourseAdmin()
  const navigate = useNavigate()
  const location = useLocation()
  const [activeTab, setActiveTab] = useState(location.hash.slice(1) || "description")

  const CodeBlock = {
    code({ children, className, node, ...rest }: any) {
      const match = /language-(\w+)/.exec(className || "")
      return match ? (
        <SyntaxHighlighter
          {...rest}
          PreTag="div"
          children={String(children).replace(/\n$/, "")}
          language={match[1]}
          style={app.theme === "light" ? oneLight : oneDark}
        />
      ) : (
        <code
          {...rest}
          className={className}
        >
          {children}
        </code>
      )
    },
  }

  const now = Date.now()
  const deadline = new Date(project?.deadline ?? "").getTime()

  const items: TabsProps["items"] = useMemo(() => {
    const items: TabsProps["items"] = [
      {
        key: "description",
        label: t("home.projects.description"),
        children: project && <Markdown components={CodeBlock}>{project.description}</Markdown>,
      },
      {
        key: "groups",
        label: t("course.groups"),
        children: <GroupTab />,
      },
      {
        key: "submissions",
        label: t("project.submissions"),
        children: courseAdmin ? (
          <Tooltip title={now > deadline ? t("project.deadlinePassed") : ""}>
            <span>
              <SubmissionsTab />
            </span>
          </Tooltip>
        ) : (
          <SubmissionCard
            projectId={Number(projectId)}
            courseId={course.courseId}
          />
        ),
      },
      {
        key: "score",
        label: t("course.score"),
        children: <ScoreCard />,
      },
    ]
    return items
  }, [project, course, courseAdmin])

  const changeTab = (key: string) => {
    navigate(`#${key}`)
    setActiveTab(key)
  }

  const handleNewSubmission = () => {
    navigate(AppRoutes.NEW_SUBMISSION.replace(AppRoutes.PROJECT + "/", ""))
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
            <Link to="edit">
              <Button
                type="primary"
                icon={<SettingFilled />}
              >
                {t("project.options")}
              </Button>
            </Link>
          ) : (
            <Button
              disabled={now < deadline}
              type="primary"
              onClick={handleNewSubmission}
              icon={<PlusOutlined />}
            >
              {t("project.newSubmission")}
            </Button>
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
