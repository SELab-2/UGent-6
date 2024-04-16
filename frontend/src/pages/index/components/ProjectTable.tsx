import { Button, Space, Table, TableProps } from "antd"
import { FC, useMemo } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import { useTranslation } from "react-i18next"
import useAppApi from "../../../hooks/useAppApi"
import ProjectInfo from "./ProjectInfo"
import ProjectStatusTag from "./ProjectStatusTag"
import GroupProgress from "./GroupProgress"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const ProjectTable: FC<{ projects: ProjectType[]|null,ignoreColumns?: string[] }> = ({ projects,ignoreColumns }) => {
  const { t } = useTranslation()
  const { modal } = useAppApi()

  const columns: TableProps<ProjectType>["columns"] = useMemo(
    () => {
      let columns:TableProps<ProjectType>["columns"] = [
      {
        title: t("home.projects.name"),
        key: "name",
        render: (project:ProjectType) => (
            <Link to={AppRoutes.PROJECT.replace(":courseId", project.course.courseId + "").replace(":projectId", project.projectId + "")}>
              <Button
                type="link"
                style={{ fontWeight: "bold" }}
              >
                {project.name}
              </Button>
            </Link>
          )
      },
      {
        title: t("home.projects.course"),
        dataIndex: "course",
        key: "course",
        render: (course: ProjectType["course"]) => course.name,
      },
      {
        title: t("home.projects.deadline"),
        dataIndex: "deadline",
        key: "deadline",
        render: (text: string) =>
          new Date(text).toLocaleString(undefined, {
            year: "numeric",
            month: "long",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          }),
      },

      {
        // volcano, geekblue,green
        title: t("home.projects.projectStatus"),
        key:"status",
        render: (project:ProjectType) =>
          !project.status ? (
            <GroupProgress
              usersCompleted={project.progress.completed}
              userCount={project.progress.total}
            />
          ) : <ProjectStatusTag status={project.status} />, 
      },
      {
        key: "action",
        render: (e) => (
          <Space size="middle">
            <Button
              onClick={() =>
                modal.info({
                  width: "1000px",
                  
                  styles: {
                   
                  },
                  title: e.name,
                  content: <ProjectInfo project={e} />,
                })
              }
              type="link"
            >
              {t("home.projects.showMore")}
            </Button>

            {/* {!isTeacher && <Button type="link">{t("home.projects.submit")}</Button>} */}
          </Space>
        ),
      },
    ]
  
    if(ignoreColumns) {
      columns  = columns.filter((c) => !ignoreColumns.includes(c.key as string))
    }
    return columns
  },
    [t, modal, projects]
  )


  return (
    <Table
      locale={{
        emptyText: t("home.projects.noProjects"),
      }}
      loading={projects == null}
      dataSource={projects??[]}
      columns={columns}
    />
  )
}

export default ProjectTable
