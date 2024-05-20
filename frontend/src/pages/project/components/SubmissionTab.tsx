import { FC, useEffect, useState } from "react"
import SubmissionList from "./SubmissionList"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import useProject from "../../../hooks/useProject"
import useApi from "../../../hooks/useApi"

export type GroupSubmissionType = GET_Responses[ApiRoutes.PROJECT_GROUP_SUBMISSIONS][number]

const SubmissionTab: FC<{ projectId: number; courseId: number; testSubmissions?: boolean }> = ({ projectId, courseId, testSubmissions }) => {
  const [submissions, setSubmissions] = useState<GroupSubmissionType[] | null>(null)
  const project = useProject()
  const API = useApi()

  useEffect(() => {
    if (!project) return
    if (!project.submissionUrl) return setSubmissions([])
    if (!project.groupId && !testSubmissions) return console.error("No groupId found")
    console.log(project)
    let ignore = false
    console.log("Sending request to: ", project.submissionUrl)
    API.GET(testSubmissions ? ApiRoutes.PROJECT_TEST_SUBMISSIONS : ApiRoutes.PROJECT_GROUP_SUBMISSIONS, { pathValues: { projectId: project.projectId, groupId: project.groupId ?? "" } }).then((res) => {
      console.log(res)
      if (!res.success || ignore) return
      setSubmissions(res.response.data.sort((a, b) => b.submissionId - a.submissionId))
    })

    return () => {
      ignore = true
    }
  }, [projectId, courseId, project?.groupId])

  return (
    <>
 

      <SubmissionList submissions={submissions} />
    </>
  )
}

export default SubmissionTab
