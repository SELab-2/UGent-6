import Typography from "antd/es/typography/Typography"
import { useEffect, useLayoutEffect } from "react"
import { useTranslation } from "react-i18next"
import { useParams } from "react-router-dom"
import useApi from "../../../hooks/useApi"
import { ApiRoutes } from "../../../@types/requests.d"

const ExtraFilesDownload = () => {
  const { t } = useTranslation()
  const { projectId } = useParams()
  const API = useApi()

  useLayoutEffect(() => {
    if (!projectId) return console.error("No projectId found")
    const downloadFile = async () => {
      try {
        const res = await API.GET(ApiRoutes.PROJECT_TESTS_UPLOAD, { pathValues: { id: projectId }, config: { responseType: "blob" } }, "page")
        if (!res.success) {
          return
        }
        const res2 = await API.GET(ApiRoutes.PROJECT_TESTS, { pathValues: { id: projectId } })
        if (!res2.success) return
        const filename = res2.response.data.extraFilesName
        const response = res.response
        const url = window.URL.createObjectURL(response.data)
        const link = document.createElement("a")
        link.href = url
        link.setAttribute("download", filename) // or any other extension
        document.body.appendChild(link)
        link.click()
        link.parentNode!.removeChild(link)
      } catch (err) {
        console.error(err)
      }
    }

    downloadFile()
  }, [projectId])

  return (
    <div style={{ margin: "3rem", textAlign: "center", width: "100%" }}>
      <Typography>{t("project.downloadingFile")}</Typography>
    </div>
  )
}

export default ExtraFilesDownload
