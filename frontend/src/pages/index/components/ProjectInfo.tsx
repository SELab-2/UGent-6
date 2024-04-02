import { FC, useMemo } from "react"
import { ProjectType } from "./ProjectTable"
import { Descriptions, DescriptionsProps } from "antd"
import { useTranslation } from "react-i18next"
import ProjectStatusTag from "./ProjectStatusTag"
import GroupProgress from "./GroupProgress"



const ProjectInfo:FC<{project:ProjectType}> = ({project}) => {

  const {t} = useTranslation()

  const items: DescriptionsProps['items'] = useMemo(()=> [
    {
      label: t("home.projects.name"),
      children: project.course.name,
      span: 24
    },
    {
      label:t("home.projects.deadline"),
      children: new Date(project.deadline).toLocaleString(undefined, {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }),
      span: 24
    },
    {
      label: t("home.projects.description"),
      children: project.description,
      span: 24
    },
    {
      label: t("home.projects.projectStatus"),
      children: <ProjectStatusTag status={project.state} />,
      span: 24
    },
    {
      label: t("home.projects.groupProgress"),
      children: <GroupProgress usersCompleted={project.progress.usersCompleted} userCount={project.progress.userCount} />,
      span: 24
    }
  ],[project, t])

  return <Descriptions column={24} bordered items={items} />
}

export default ProjectInfo