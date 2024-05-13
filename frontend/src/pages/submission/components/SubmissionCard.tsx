import { Card, Spin, theme, Input, Button, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { GET_Responses } from "../../../@types/requests"
import { ApiRoutes } from "../../../@types/requests"
import { ArrowLeftOutlined } from "@ant-design/icons"
import { useNavigate } from "react-router-dom"
import "@fontsource/jetbrains-mono"
import useApi from "../../../hooks/useApi"

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const API = useApi()

  const navigate = useNavigate()


  const downloadSubmission = async () => {
    const response = await API.GET(submission.fileUrl, { })
    if(!response.success) return
    const fileContent = response.response
    console.log(fileContent)
    const blob = new Blob([fileContent.data], { type: "text/plain" })
    const url = URL.createObjectURL(blob)
    const link = document.createElement("a")
    link.href = url
    link.download = "indiening.zip"
    document.body.appendChild(link)
    link.click()
    URL.revokeObjectURL(url)
    document.body.removeChild(link)
  
  }

  //TODO: better way to show submission feedback
  // const feedbacksubmission.dockerFeedback.feedback
  const feedback = "TODO: feedback"
  return (
    <Card
      styles={{
        header: {
          background: token.colorPrimaryBg,
        },
        title: {
          fontSize: "1.1em",
        },
      }}
      type="inner"
      title={
        <span>
          <Button
            onClick={() => navigate(-1)}
            type="text"
            style={{ marginRight: 16 }}
          >
            <ArrowLeftOutlined />
          </Button>
          {t("submission.submission")}
        </span>
      }
    >
      {t("submission.submittedFiles")}

      <ul style={{ listStyleType: "none" }}>
        <li>
          <Button
            type="link"
            style={{ padding: 0 }}
            onClick={downloadSubmission}
          >
            <u>indiening.zip</u>
          </Button>
        </li>
      </ul>

      {t("submission.structuretest")}

      <ul style={{ listStyleType: "none" }}>
        <li>
          <Typography.Text type={submission.structureAccepted ? "success" : "danger"}>{submission.structureAccepted ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
          {submission.structureAccepted ? null : (
            <div>
              {submission.structureFeedback === null ? (
                <Spin />
              ) : (
                <Input.TextArea
                  readOnly
                  value={submission.structureFeedback}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono", marginTop: 8 }}
                  rows={4}
                  autoSize={{ minRows: 4, maxRows: 8 }}
                />
              )}
            </div>
          )}
        </li>
      </ul>

      {t("submission.dockertest")}

      <ul style={{ listStyleType: "none" }}>
        <li>
          <Typography.Text type={submission.dockerAccepted ? "success" : "danger"}>{submission.dockerAccepted ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
          {submission.dockerAccepted ? null : (
            <div>
              {submission.dockerFeedback === null ? (
                <Spin />
              ) : (
                <Input.TextArea
                  readOnly
                  value={feedback}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono", marginTop: 8 }}
                  rows={4}
                  autoSize={{ minRows: 4, maxRows: 16 }}
                />
              )}
            </div>
          )}
        </li>
      </ul>
    </Card>
  )
}

export default SubmissionCard
