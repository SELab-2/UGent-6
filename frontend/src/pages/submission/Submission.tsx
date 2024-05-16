import { Spin } from "antd"
import { useEffect, useState } from "react"
import SubmissionCard from "./components/SubmissionCard"
import { SubmissionType } from "./components/SubmissionCard"
import { useParams } from "react-router-dom"
import { ApiRoutes } from "../../@types/requests.d"
import useApi from "../../hooks/useApi"

const Submission = () => {
  const [submission, setSubmission] = useState<SubmissionType | null>(null)
  const { submissionId } = useParams()
  const API = useApi()

  useEffect(() => {
    if (!submissionId) return console.error("No submissionId found")
      let ignore = false
    API.GET(ApiRoutes.SUBMISSION, { pathValues: { id: submissionId } }).then((res) => {
      if (!res.success || ignore) return
      setSubmission(res.response.data)
    })
    return () => {
      ignore = true
    }
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
