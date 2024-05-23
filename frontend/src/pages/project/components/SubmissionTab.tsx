import { FC, useEffect, useState } from "react"
import SubmissionList from "./SubmissionList"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import useProject from "../../../hooks/useProject"
import useApi from "../../../hooks/useApi"

export type GroupSubmissionType = GET_Responses[ApiRoutes.PROJECT_GROUP_SUBMISSIONS][number]

const SubmissionTab: FC<{ projectId: number; courseId: number; testSubmissions?: boolean }> = ({ projectId, courseId, testSubmissions }) => {
  const [submissions, setSubmissions] = useState<GroupSubmissionType[] | null>(null)
  const [indices, setIndices] = useState<Map<number, number>>(new Map())
  const project = useProject()
  const API = useApi()

  useEffect(() => {
    if (!project) return
    if (!project.submissionUrl) return setSubmissions([])
    if (!project.groupId && !testSubmissions) return console.error("No groupId found")
    let ignore = false
    API.GET(testSubmissions ? ApiRoutes.PROJECT_TEST_SUBMISSIONS : ApiRoutes.PROJECT_GROUP_SUBMISSIONS, { pathValues: { projectId: project.projectId, groupId: project.groupId ?? "" } }).then((res) => {
      if (!res.success || ignore) return
      //this is sorts the submissions by submission time, with the oldest submission first
      const ascending = res.response.data.sort((a, b) => new Date(a.submissionTime).getTime() - new Date(b.submissionTime).getTime())
      const tmp = new Map()
      ascending.forEach((submission, index) => {
        tmp.set(submission.submissionId, index+1)
      })
      setIndices(tmp)
      //we need descending order, so we reverse the array
      setSubmissions(ascending.reverse())
    })

    return () => {
      ignore = true
    }
  }, [projectId, courseId, project?.groupId])

  return (
    <>
 

      <SubmissionList submissions={submissions} indices={indices} />
    </>
  )
}

export default SubmissionTab
