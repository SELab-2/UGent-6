import { Button, List, Space, Table, TableProps, Tag } from "antd"
import { FC } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import { useTranslation } from "react-i18next"
import Typography from "antd/es/typography/Typography"

export type ProjectType = GET_Responses[ApiRoutes.PROJECT]





const ProjectTable:FC<{projects:ProjectType[]}> = ({projects}) => {
  const {t} = useTranslation()


  const columns: TableProps<ProjectType>['columns'] = [
    {
      title: t("home.projects.name"),
      dataIndex: 'name',
      key: 'name',
      render: (text:string) => <Button type="link" style={{fontWeight:"bold"}}>{text}</Button>,
    },
    {
      title: t("home.projects.course"),
      dataIndex: 'course',
      key: 'course',
    },
    {
      title: t("home.projects.deadline"),
      dataIndex: 'deadline',
      key: 'deadline',
      render: (text:string) => new Date(text).toLocaleString(undefined, {
        year: 'numeric', 
        month: 'long', 
        day: 'numeric', 
        hour: '2-digit', 
        minute: '2-digit', 
      })
    },
   
    { // volcano, geekblue,green
      title: t("home.projects.status"),
      key: 'status',
      render: () => ( Math.random() >  0.5 ? 
        <Tag color="green">
          {t("home.projects.statusDone")}
      </Tag>:<Tag color="volcano">
          {t("home.projects.statusNotDone")}
      </Tag>
      ),
    },
    {
      key: 'action',
      render: () => (
        <Space size="middle">
          <Button type="link">
          {t("home.projects.showMore")}
          </Button>

          <Button type="link">
          {t("home.projects.submit")}
          </Button>
        </Space>
      ),
    },
  ];


  return <Table locale={{
    emptyText: t("home.projects.noProjects")
  }} dataSource={projects}  columns={columns} />
}

export default ProjectTable
