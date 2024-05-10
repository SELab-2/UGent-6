import { Card, Typography, Spin } from "antd"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import SubmissionCard from "./components/SubmissionCard"
import { SubmissionType } from "./components/SubmissionCard"
import { useParams } from "react-router-dom"
import apiCall from "../../util/apiFetch"
import { ApiRoutes } from "../../@types/requests.d"

const Submission = () => {
  const [submission, setSubmission] = useState<SubmissionType | null>(null)
  const { submissionId } = useParams()

  useEffect(() => {
    if (!submissionId) return console.error("No submissionId found")
    apiCall.get(ApiRoutes.SUBMISSION, { id: submissionId }).then((res) => {
      console.log(res.data)
      setSubmission(res.data)
    })
  }, [submissionId])

  if (submission === null) {
    return (
      <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin
          tip="Loading..."
          size="large"
        />
      </div>
    )
  }

  return (
    <div style={{ margin: "3rem 0" }}>
      <SubmissionCard submission={submission} />
    </div>
  )
}

export default Submission
