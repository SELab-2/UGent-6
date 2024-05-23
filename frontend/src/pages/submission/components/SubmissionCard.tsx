import { Card, theme, Button, Space } from "antd"
import { useTranslation } from "react-i18next"
import { GET_Responses,ApiRoutes } from "../../../@types/requests.d"
import { ArrowLeftOutlined, DownloadOutlined } from "@ant-design/icons"
import { useLocation, useNavigate } from "react-router-dom"
import "@fontsource/jetbrains-mono"
import SubmissionContent from "./SubmissionCardContent"
import useApi from "../../../hooks/useApi"

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const API = useApi()

  const location = useLocation()
  const index = new URLSearchParams(location.search).get("index")

  console.log(submission);
  const downloadFile = async (route: ApiRoutes.SUBMISSION_FILE | ApiRoutes.SUBMISSION_ARTIFACT, filename: string) => {

    const response = await API.GET(
      route.replace("/api/","/web/api/") as ApiRoutes.SUBMISSION_FILE,
      {
        config: {
          responseType: "blob",
          transformResponse: [(data) => data],
        },
       
      },
      "message"
    )
    if (!response.success) return
    const url = window.URL.createObjectURL(new Blob([response.response.data]))
    const link = document.createElement("a")
    link.href = url
    let fileName = filename+".zip" // default filename
    link.setAttribute("download", fileName)
    document.body.appendChild(link)
    link.click()
    link.parentNode!.removeChild(link)

}
  
  const downloadSubmission = async () => {
    downloadFile(submission.fileUrl, t("project.submission"))
  }

  const downloadSubmissionArtifacts = async () => {
    downloadFile(submission.artifactUrl!, t("project.submissionArtifacts"))
  }

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
      title= {t("submission.submission") + (index ? ` #${index}` : "") }
      extra={<Space>

        {submission.fileUrl && <Button key="file" type="primary" icon={<DownloadOutlined/>} onClick={downloadSubmission}>{t("submission.downloadSubmission")}</Button>}
        {submission.artifactUrl && <Button key="artifacts" type="primary" icon={<DownloadOutlined/>} onClick={downloadSubmissionArtifacts}>{t("submission.downloadArtifacts")}</Button>}
      </Space>
      }
    >
      <SubmissionContent submission={submission} />
    </Card>
  )
}

export default SubmissionCard
