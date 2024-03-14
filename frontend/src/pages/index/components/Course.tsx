import { ContainerOutlined, TeamOutlined } from "@ant-design/icons"
import { Card, List, Statistic, Tooltip, theme } from "antd"
import { FC } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import ProjectStatusTag, { ProjectStatus } from "./ProjectStatusTag"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"

type CourseType = GET_Responses[ApiRoutes.COURSE]

const Course: FC<{ course: CourseType }> = ({ course }) => {
  const { t } = useTranslation()
  const { token } = theme.useToken()
  const navigate = useNavigate()

  return (
    <Card
      styles={{
        header: {
          background: token.colorPrimaryBg,
        },
        title: {
          fontSize: "1.1em",
        },
      }}
      bordered={false}
      hoverable
      onClick={() => navigate(AppRoutes.COURSE.replace(":id", course.id.toString()))}
      type="inner"
      title={course.name}
      style={{ width: 300 }}
      actions={[
        <Tooltip title={t("home.projects.userCourseCount", { count: 2 })}>
          <span>
            <Statistic
              valueStyle={{ fontSize: "1em", color: token.colorTextLabel }}
              prefix={<TeamOutlined />}
              value={72}
            />
          </span>
        </Tooltip>,

        <Tooltip title={t("home.projects.activeProjects_plural", { count: 2 })}>
          <span>
            <Statistic
              valueStyle={{ fontSize: "1em", color: token.colorTextLabel }}
              prefix={<ContainerOutlined />}
              value={2}
            />
          </span>
        </Tooltip>,
      ]}
    >
      <List>
        <List.Item actions={[<ProjectStatusTag status={["processing", "completed", "notStarted"][Math.floor(Math.random() * 3)] as ProjectStatus} />]}>
          <List.Item.Meta title={"Opdracht " + Math.floor(Math.random() * 100 + 1)} />
        </List.Item>

        <List.Item actions={[<ProjectStatusTag status={["processing", "completed", "notStarted"][Math.floor(Math.random() * 3)] as ProjectStatus} />]}>
          <List.Item.Meta title={"Opdracht " + Math.floor(Math.random() * 100 + 1)} />
        </List.Item>
      </List>
    </Card>
  )
}

export default Course
