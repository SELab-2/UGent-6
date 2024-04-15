import { CheckCircleOutlined, CloseCircleOutlined, MinusCircleOutlined } from "@ant-design/icons"
import { Tag } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { ProjectStatus } from "../../../@types/requests.d"


const ProjectStatusTag: FC<{ status: ProjectStatus,icon?:boolean }> = ({ status,icon }) => {
  const { t } = useTranslation()

  if(icon){
    if (status === "correct") {
      return <Tag icon={<CheckCircleOutlined />} color="green">{t("home.projects.status.completed")}</Tag>
    } else if (status === "incorrect") {
      return <Tag icon={<CloseCircleOutlined />} color="volcano">{t("home.projects.status.failed")}</Tag>
    } else if (status === "not started") {
      return <Tag icon={<MinusCircleOutlined />} color="default">{t("home.projects.status.notStarted")}</Tag>
    } else return null
  }

  if (status === "correct") {
    return <Tag color="green">{t("home.projects.status.completed")}</Tag>
  } else if (status === "incorrect") {
    return <Tag color="volcano">{t("home.projects.status.failed")}</Tag>
  } else if (status === "not started") {
    return <Tag color="default">{t("home.projects.status.notStarted")}</Tag>
  } else return null
}

export default ProjectStatusTag
