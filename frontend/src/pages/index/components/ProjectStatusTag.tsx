import { Tag } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"

export type ProjectStatus = "completed" | "processing" | "notStarted"

const ProjectStatusTag: FC<{ status: ProjectStatus }> = ({ status }) => {
  const { t } = useTranslation()

  if (status === "completed") {
    return <Tag color="green">{t("home.projects.status.completed")}</Tag>
  } else if (status === "processing") {
    return <Tag color="geekblue">{t("home.projects.status.processing")}</Tag>
  } else if (status === "notStarted") {
    return <Tag color="volcano">{t("home.projects.status.notStarted")}</Tag>
  } else return null
}

export default ProjectStatusTag
