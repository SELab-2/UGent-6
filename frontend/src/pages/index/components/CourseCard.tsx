import { ContainerOutlined, TeamOutlined } from "@ant-design/icons"
import { Card, List, Statistic, Tooltip, theme } from "antd"
import { FC } from "react"
import ProjectStatusTag from "./ProjectStatusTag"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { UserCourseType } from "../../../providers/UserProvider"


const CourseCard: FC<{ course: UserCourseType }> = ({ course }) => {
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
      onClick={() => navigate(AppRoutes.COURSE.replace(":courseId", course.courseId.toString()))}
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
        <List.Item actions={[<ProjectStatusTag key="status" icon status={"failed"} />]}>
          <List.Item.Meta title={"Opdracht " + Math.floor(Math.random() * 100 + 1)} />
        </List.Item>

        <List.Item actions={[<ProjectStatusTag key="status" icon status={"notStarted"}/>]}>
          <List.Item.Meta title={"Opdracht " + Math.floor(Math.random() * 100 + 1)} />
        </List.Item>
      </List>
    </Card>
  )
}

export default CourseCard
