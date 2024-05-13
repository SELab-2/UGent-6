import { FC, useEffect, useState } from "react"
import SubmissionList from "./SubmissionList"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import { useParams } from "react-router-dom"
import useProject from "../../../hooks/useProject"

export type GroupSubmissionType = GET_Responses[ApiRoutes.PROJECT_GROUP_SUBMISSIONS][number]

const SubmissionTab: FC<{ projectId: number; courseId: number }> = ({ projectId, courseId }) => {
  const [submissions, setSubmissions] = useState<GroupSubmissionType[] | null>(null)
  const project = useProject()

  useEffect(() => {

    if(!project) return 
    console.log(project.submissionUrl);
    apiCall.get(project.submissionUrl ).then((res) => {
      
      setSubmissions(res.data.sort((a, b) => b.submissionId - a.submissionId))
    })



  }, [projectId,courseId])



  return (<SubmissionList submissions={submissions} />
  )
}

export default SubmissionTab
