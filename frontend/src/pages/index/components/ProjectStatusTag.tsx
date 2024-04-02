import { CheckCircleOutlined, CheckOutlined, ClockCircleOutlined, CloseCircleOutlined, LoadingOutlined, MinusCircleOutlined } from "@ant-design/icons"
import { Tag } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { ProjectStatus } from "../../../@types/requests"


const ProjectStatusTag: FC<{ status: ProjectStatus,icon?:boolean }> = ({ status,icon }) => {
  const { t } = useTranslation()

  if(icon){
    if (status === "completed") {
      return <Tag icon={<CheckCircleOutlined />} color="green">{t("home.projects.status.completed")}</Tag>
    } else if (status === "failed") {
      return <Tag icon={<CloseCircleOutlined />} color="volcano">{t("home.projects.status.failed")}</Tag>
    } else if (status === "notStarted") {
      return <Tag icon={<MinusCircleOutlined />} color="default">{t("home.projects.status.notStarted")}</Tag>
    } else return null
  }

  if (status === "completed") {
    return <Tag color="green">{t("home.projects.status.completed")}</Tag>
  } else if (status === "failed") {
    return <Tag color="volcano">{t("home.projects.status.failed")}</Tag>
  } else if (status === "notStarted") {
    return <Tag color="default">{t("home.projects.status.notStarted")}</Tag>
  } else return null
}

export default ProjectStatusTag
