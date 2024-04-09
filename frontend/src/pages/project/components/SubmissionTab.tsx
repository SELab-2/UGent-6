import { FC, useEffect, useState } from "react"
import SubmissionList, { SubmissionType } from "./SubmissionList"
import { Button, Card } from "antd"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { PlusOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"

const SubmissionTab: FC<{ projectId: number; courseId: number }> = ({ projectId, courseId }) => {
  const [submissions, setSubmissions] = useState<SubmissionType[] | null>(null)

  useEffect(() => {
    //TODO: fetch submissions
    

    apiCall.get(ApiRoutes.PROJECT_SUBMISSIONS, {id: projectId}).then((res) => {
      console.log(res.data)
      setSubmissions(res.data)
    })



  }, [projectId,courseId])



  return (<SubmissionList submissions={submissions} />
  )
}

export default SubmissionTab
