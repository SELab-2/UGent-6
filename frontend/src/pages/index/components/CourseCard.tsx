import { ContainerOutlined, TeamOutlined } from "@ant-design/icons"
import { Card, List, Statistic, Tooltip, Typography, theme } from "antd"
import { FC } from "react"
import ProjectStatusTag from "./ProjectStatusTag"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import GroupProgress from "./GroupProgress"
import { CourseProjectsType } from "./CourseSection"
import { Link } from "react-router-dom"

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
        <Tooltip title={t(courseProjects.course.memberCount > 1? "home.projects.userCourseCount_plural": "home.projects.userCourseCount", { count: courseProjects.course.memberCount })}>
          <span>
            <Statistic
              valueStyle={{ fontSize: "1em", color: token.colorTextLabel }}
              prefix={<TeamOutlined />}
              value={courseProjects.course.memberCount}
            />
          </span>
        </Tooltip>,

        <Tooltip title={t(courseProjects.projects.length > 1 ? "home.projects.activeProjects_plural": "home.projects.activeProjects", { count:courseProjects.projects.length })}>
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
        dataSource={courseProjects.projects.slice(0, 3)}
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
            <List.Item.Meta title={
              <Link to={AppRoutes.PROJECT.replace(":courseId", courseProjects.course.courseId.toString()).replace(":projectId", project.projectId.toString())} style={{ color: token.colorPrimary }}
                onClick={(event) => event.stopPropagation()}>
                <Typography.Text ellipsis>{project.name}</Typography.Text>
              </Link>}/>
          </List.Item>
        )}
      >
        {courseProjects.projects.length > 0 && <Typography.Text style={{ textAlign: 'left', display: 'block' }}>...</Typography.Text>}
      </List>
    </Card>
  )
}

export default CourseCard
