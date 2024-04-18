import { ContainerOutlined, TeamOutlined } from "@ant-design/icons"
import { Card, List, Statistic, Tooltip, theme } from "antd"
import { FC } from "react"
import ProjectStatusTag from "./ProjectStatusTag"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { CourseProjectsType } from "./HorizontalCourseScroll"
import GroupProgress from "./GroupProgress"

const CourseCard: FC<{ courseProjects: CourseProjectsType[string], adminView?:boolean }> = ({ courseProjects,adminView }) => {
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
        body: {
          height:"calc(100% - 6rem)",
        }
      }}

      bordered={false}
      hoverable
      onClick={() => navigate(AppRoutes.COURSE.replace(":courseId", courseProjects.course.courseId.toString()))}
      type="inner"
      title={courseProjects.course.name}
      style={{ width: 300,height:"100%" }}
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

        <Tooltip title={t("home.projects.activeProjects_plural", { count:courseProjects.projects.length })}>
          <span>
            <Statistic
              valueStyle={{ fontSize: "1em", color: token.colorTextLabel }}
              prefix={<ContainerOutlined />}
              value={courseProjects.projects.length}
            />
          </span>
        </Tooltip>,
      ]}
    >
      <List
        dataSource={courseProjects.projects}
        locale={{ emptyText: t("home.projects.noProjects") }}
        rowKey="projectId"
        renderItem={(project) => (
          <List.Item
            actions={[
              project.status ? (
                <ProjectStatusTag
                  key="status"
                  icon
                  status={project.status}
                />
              ): <GroupProgress
              usersCompleted={project.progress.completed}
              userCount={project.progress.total}
            />,
            ]}
          >
            <List.Item.Meta title={project.name} />
          </List.Item>
        )}
      ></List>
    </Card>
  )
}

export default CourseCard
