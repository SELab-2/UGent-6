import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import SubmissionsTable from "./SubmissionsTable"
import apiCall from "../../../util/apiFetch"
import { useParams } from "react-router-dom"

export type ProjectSubmissionsType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS][number]

// Card of all the latests submissions for a project
const SubmissionsTab = () => {
  const [submissions, setSubmissions] = useState<ProjectSubmissionsType[] | null>(null)
  const { projectId } = useParams()
  useEffect(() => {
    // TODO: make request to /projects/{projectid}/submissions
    if(!projectId) return
    apiCall.get(ApiRoutes.PROJECT_SUBMISSIONS,{id:projectId}).then((res) => {
      console.log(res.data)
      setSubmissions(res.data)

    })
  }, [])

  const handleDownloadSubmissions = () => {}

  return (
    <>
      <SubmissionsTable submissions={submissions} />
    </>
  )
}

export default SubmissionsTab
