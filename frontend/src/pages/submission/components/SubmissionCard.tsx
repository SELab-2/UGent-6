import { Card, Spin, theme, Input, Button, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { GET_Responses } from "../../../@types/requests"
import { ApiRoutes } from "../../../@types/requests"
import { ArrowLeftOutlined, DownloadOutlined } from "@ant-design/icons"
import { useNavigate } from "react-router-dom"
import "@fontsource/jetbrains-mono"
import apiCall from "../../../util/apiFetch"
import SubmissionContent from "./SubmissionCardContent"

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { token } = theme.useToken()
  const { t } = useTranslation()
  const navigate = useNavigate()

  
  const downloadSubmission = async () => {
    try {
      const response = await apiCall.get(submission.fileUrl, undefined, undefined, {
        responseType: "blob",
        transformResponse: [(data) => data],
      })
      console.log(response)
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement("a")
      link.href = url
      const contentDisposition = response.headers["content-disposition"]
      console.log(contentDisposition)
      let fileName = "file.zip" // default filename
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename=([^;]+)/)
        console.log(fileNameMatch)
        if (fileNameMatch && fileNameMatch[1]) {
          fileName = fileNameMatch[1] // use the filename from the headers
        }
      }
      link.setAttribute("download", fileName)
      document.body.appendChild(link)
      link.click()
    } catch (err) {
      console.error(err)
    }
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
      extra={
        <Button type="primary" icon={<DownloadOutlined/>} onClick={downloadSubmission}>{t("submission.downloadSubmission")}</Button>
      }
    >
      <SubmissionContent submission={submission} />
    </Card>
  )
}

export default SubmissionCard
