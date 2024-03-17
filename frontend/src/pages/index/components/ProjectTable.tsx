import { Button,  Space, Table, TableProps } from "antd"
import { FC, useMemo } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import { useTranslation } from "react-i18next"
import useApiApp from "../../../hooks/useApiApp"
import ProjectInfo from "./ProjectInfo"
import ProjectStatusTag from "./ProjectStatusTag"
import useIsTeacher from "../../../hooks/useIsTeacher"
import GroupProgress from "./GroupProgress"

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]

const ProjectTable: FC<{ projects: ProjectType[] }> = ({ projects }) => {
  const { t } = useTranslation()
  const {modal} = useApiApp()
  const isTeacher=  useIsTeacher()

  const columns: TableProps<ProjectType>["columns"] = useMemo(() => [
    {
      title: t("home.projects.name"),
      dataIndex: "name",
      key: "name",
      render: (text: string) => (
        <Button
          type="link"
          style={{ fontWeight: "bold" }}
        >
          {text}
        </Button>
      ),
    },
    {
      title: t("home.projects.course"),
      dataIndex: "course",
      key: "course",
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
      title: isTeacher ? t("home.projects.groupProgress")  : t("home.projects.projectStatus"),
      key: "status",
      render: () => isTeacher ? <GroupProgress usersCompleted={Math.floor(Math.random() * 121)} userCount={121}/> : (Math.random() > 0.5 ? <ProjectStatusTag status="completed" /> : <ProjectStatusTag status="notStarted" />),
    },
    {
      key: "action",
      render: (e) => (
        <Space size="middle">
          <Button onClick={() => modal.info({
            styles: {
              content: {
                width: "600px",
              }
            },
            title: e.name,
            content: <ProjectInfo project={e} />
          })} type="link">{t("home.projects.showMore")}</Button>

         {!isTeacher && <Button type="link">{t("home.projects.submit")}</Button>}
        </Space>
      ),
    }
  ],[t, modal,isTeacher])

  return (
    <Table
      locale={{
        emptyText: t("home.projects.noProjects"),
      }}
      dataSource={projects.map((p) => ({ ...p, key: p.id }))}
      columns={columns}
    />
  )
}

export default ProjectTable
