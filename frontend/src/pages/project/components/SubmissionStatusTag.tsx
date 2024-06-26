import { Tag } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import { LoadingOutlined } from "@ant-design/icons"


export enum SubmissionStatus {
  STRUCTURE_REJECTED = 0,
  DOCKER_REJECTED = 1<<1,
  NOT_SUBMITTED = 1<<2,
  PASSED = 1<<3,
  RUNNING = 1<<4
}

export function createStatusBitVector(submission: GET_Responses[ApiRoutes.SUBMISSION] | null) {
  if(submission === null) return SubmissionStatus.NOT_SUBMITTED
  let status = 0
  if(submission.dockerStatus === "running") {
    status |= SubmissionStatus.RUNNING
  }
  if(!submission.structureAccepted){
    status |= SubmissionStatus.STRUCTURE_REJECTED
  }
  if(!submission.dockerFeedback.allowed){
    status |= SubmissionStatus.DOCKER_REJECTED
  }
  if(status === 0){
    status |= SubmissionStatus.PASSED
  }
  return status
}


const SubmissionStatusTag:FC<{status:number}> = ({ status }) => {
  const {t} = useTranslation()
  if((status & SubmissionStatus.DOCKER_REJECTED)){
    return (
      <Tag color="red">{t("project.testFailed")}</Tag>
    )
  } else if (status & SubmissionStatus.STRUCTURE_REJECTED) {
    return (
      <Tag color="red">{t("project.structureFailed")}</Tag>
    )
  } else if (status & SubmissionStatus.NOT_SUBMITTED) {
    return (
      <Tag color="gray">{t("project.notSubmitted")}</Tag>
    )
  } else if (status & SubmissionStatus.RUNNING) {
    return (
      <Tag color="processing" icon={<LoadingOutlined />} >{t("project.running")}</Tag>
    )
  }

  return <Tag color="green">{t("project.passed")}</Tag>
}

export default SubmissionStatusTag