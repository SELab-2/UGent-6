import {Button, Card, Popconfirm, Tabs, TabsProps, Tooltip, theme, Tag} from "antd"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { Link, useLocation, useNavigate, useParams } from "react-router-dom"
import SubmissionCard from "./components/SubmissionTab"
import useCourse from "../../hooks/useCourse"
import useProject from "../../hooks/useProject"
import ScoreCard from "./components/ScoreTab"
import {
    ClockCircleOutlined,
    DeleteOutlined, EyeInvisibleOutlined, EyeOutlined,
    FileDoneOutlined,
    InfoCircleOutlined,
    PlusOutlined,
    SendOutlined,
    SettingFilled, StarOutlined,
    TeamOutlined
} from "@ant-design/icons"
import { useMemo, useState } from "react"
import useIsCourseAdmin from "../../hooks/useIsCourseAdmin"
import GroupTab from "./components/GroupTab"
import { AppRoutes } from "../../@types/routes"
import SubmissionsTab from "./components/SubmissionsTab"
import MarkdownTextfield from "../../components/input/MarkdownTextfield"
import useApi from "../../hooks/useApi"
import i18n from "i18next";

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
              <Tag color="default" icon={<ClockCircleOutlined/>}> {new Date(project.deadline).toLocaleString(i18n.language, {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                  hour: "2-digit",
                  minute: "2-digit",
              })} </Tag>
              <Tag color="default" icon={<StarOutlined/>}> {t("home.projects.maxScore")}: {project.maxScore}</Tag>
                {courseAdmin && (
                    <Tooltip title={project?.visible ? t("home.projects.visibleStatus.visible") : project?.visibleAfter ? `${t("home.projects.visibleStatus.visibleFrom")} ${new Date(project.visibleAfter).toLocaleString(i18n.language, {
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                    })}` : t("home.projects.visibleStatus.invisible")}>
                        <Tag icon={project?.visible ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                             color="default"/>
                    </Tooltip>
                )}
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
        icon: courseAdmin ? <FileDoneOutlined />  : <SendOutlined />,
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

      if(courseAdmin) {
        items.push({
          key: "testSubmissions",
          label: t("project.testSubmissions"),
          icon: <SendOutlined />,
          children: 
            <SubmissionCard
              projectId={Number(projectId)}
              courseId={course.courseId}
              testSubmissions
            />
        })
      }

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
        <div style={{ margin: "3rem 0", marginTop: "1rem", width: "100%", paddingBottom: "3rem"}}>
  
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
                        paddingLeft: "1rem"
                    },
                }}
                style={{ width: "100%", marginBottom: "3rem" }}
                title={project?.name}
                loading={!project}
                extra={
                    courseAdmin ? (
                        <>
                            <Button
                                type="primary"
                                onClick={handleNewSubmission}
                                icon={<PlusOutlined />}
                            >
                                {t("project.newSubmissionTest")}
                            </Button>

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
                                okButtonProps={{
                                    danger: true,
                                }}
                                okText={t("course.confirmDelete")}
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
                />
            </Card>
        </div>
    )}
    export default Project;
