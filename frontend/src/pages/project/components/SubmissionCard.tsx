import { FC, useEffect, useState } from "react"
import SubmissionList, { SubmissionType } from "./SubmissionList"
import { Button, Card } from "antd"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { PlusOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"

const SubmissionCard: FC<{ projectId: number; courseId: number, allowNewSubmission?:boolean }> = ({ projectId, courseId,allowNewSubmission }) => {
  const navigate = useNavigate()
  const [submissions, setSubmissions] = useState<SubmissionType[] | null>(null)
  const { t } = useTranslation()

  useEffect(() => {
    //TODO: fetch submissions
    setTimeout(() => {
      setSubmissions([
        {
          docker_accepted: true,
          docker_feedback: "",
          file_url: "/api/submissions/1/file",
          group_url: "/api/groups/1",
          project_url: "/api/projects/1",
          structure_accepted: true,
          structure_feedback: "",
          submittionId: 1,
          submitted_time: "2024-01-01T00:00:00.000Z",
        },
        {
          docker_accepted: false,
          docker_feedback: "Failed test case 1",
          file_url: "/api/submissions/2/file",
          group_url: "/api/groups/1",
          project_url: "/api/projects/1",
          structure_accepted: true,
          structure_feedback: "",
          submittionId: 2,
          submitted_time: "2024-01-01T00:00:00.000Z",
        },
      ].slice(0,5)) // Limit to 5 submissions 
    }, 250)
  }, [])

  const handleNewSubmission = () => {
    navigate(AppRoutes.NEW_SUBMISSION.replace(AppRoutes.PROJECT + "/", ""))
  }

  return (
    <Card
      loading={!submissions}
      style={{ marginBottom: "1rem" }}
      styles={{
        body:{
          padding:"8px 16px"
        }
      }}
      extra={
        <Button
          disabled={!submissions || allowNewSubmission === false}
          type="primary"
          onClick={handleNewSubmission}
          icon={<PlusOutlined />}
        >
          {t("project.newSubmission")}
        </Button>
      }
      title={t("project.submissions")}
    >
      <SubmissionList submissions={submissions} />
    </Card>
  )
}

export default SubmissionCard
