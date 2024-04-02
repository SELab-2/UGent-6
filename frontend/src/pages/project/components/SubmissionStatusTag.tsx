import { Tag } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"



const SubmissionStatusTag:FC<{docker_accepted:boolean, structure_accepted:boolean}> = ({ docker_accepted,structure_accepted }) => {
  const {t} = useTranslation()
  if(!docker_accepted){
    return (
      <Tag color="red">{t("project.testFailed")}</Tag>
    )
  } else if (!structure_accepted) {
    return (
      <Tag color="red">{t("project.structureFailed")}</Tag>
    )
  } 

  return <Tag color="green">{t("project.passed")}</Tag>
}

export default SubmissionStatusTag