import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import SubmissionsTable from "./SubmissionsTable"
import { useParams } from "react-router-dom"
import useApi from "../../../hooks/useApi"
import { exportSubmissionStatusToCSV, exportToUfora } from "./createCsv"
import { Button, Space, Switch } from "antd"
import { DownloadOutlined, ExportOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import useProject from "../../../hooks/useProject"

export type ProjectSubmissionsType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS][number]

// Card of all the latests submissions for a project
const SubmissionsTab = () => {
  const [submissions, setSubmissions] = useState<ProjectSubmissionsType[] | null>(null)
  const { projectId } = useParams()
  const API = useApi()
  const { t } = useTranslation()
  const project = useProject()
  const [withArtifacts, setWithArtifacts] = useState<boolean>(true)

  useEffect(() => {
    if (!projectId) return
    let ignore = false
    API.GET(ApiRoutes.PROJECT_SUBMISSIONS, { pathValues: { id: projectId } }).then((res) => {
      if (!res.success || ignore) return
      console.log(res.response.data)
      setSubmissions(res.response.data)
    })
    return () => {
      ignore = true
    }
  }, [projectId])

  const handleDownloadSubmissions = async () => {
    if (!project) return
    const apiRoute = ApiRoutes.PROJECT_DOWNLOAD_ALL_SUBMISSIONS + "?artifacts=true"
    const response = await API.GET(
      apiRoute as ApiRoutes.PROJECT_DOWNLOAD_ALL_SUBMISSIONS,
      {
        config: {
          responseType: "blob",
          transformResponse: [(data) => data],
        },
        pathValues: { id: project.projectId },
      },
      "message"
    )
    if (!response.success) return
    console.log(response)
    const url = window.URL.createObjectURL(new Blob([response.response.data]))
    const link = document.createElement("a")
    link.href = url
    const fileName = `${project.name}-submissions.zip`
    link.setAttribute("download", fileName)
    document.body.appendChild(link)
    link.click()
    link.parentNode!.removeChild(link)
  }

  const handleExportToUfora = () => {
    if (!submissions || !project) return
    exportToUfora(submissions, project.maxScore ?? 0)
  }

  const exportStatus = () => {
    if (!submissions) return
    exportSubmissionStatusToCSV(submissions)
  }

  return (
    <Space
      direction="vertical"
      style={{ width: "100%" }}
    >
      <Space>
        <Switch
          checked={withArtifacts}
          checkedChildren={t("project.withArtifacts")}
          unCheckedChildren={t("project.withoutArtifacts")}
          onChange={setWithArtifacts}
        />
        <Button
          onClick={handleDownloadSubmissions}
          icon={<DownloadOutlined />}
        >
          {t("project.downloadSubmissions")}
        </Button>
        <Button
          onClick={exportStatus}
          icon={<ExportOutlined />}
        >
          {t("project.exportToCSV")}
        </Button>
        <Button
          onClick={handleExportToUfora}
          icon={<ExportOutlined />}
        >
          {t("project.exportToUfora")}
        </Button>
      </Space>
      <SubmissionsTable
        submissions={submissions}
        onChange={setSubmissions}
        withArtifacts={withArtifacts}
      />
    </Space>
  )
}

export default SubmissionsTab
