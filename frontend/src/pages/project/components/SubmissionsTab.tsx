import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import SubmissionsTable from "./SubmissionsTable"
import { useParams } from "react-router-dom"
import useApi from "../../../hooks/useApi"

export type ProjectSubmissionsType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS][number]

// Card of all the latests submissions for a project
const SubmissionsTab = () => {
  const [submissions, setSubmissions] = useState<ProjectSubmissionsType[] | null>(null)
  const { projectId } = useParams()
  const API = useApi()


  useEffect(() => {
    if(!projectId) return
    let ignore = false
    API.GET(ApiRoutes.PROJECT_SUBMISSIONS, { pathValues: { id: projectId } }).then((res) => {
      if (!res.success || ignore) return
      console.log(res.response.data)
      setSubmissions(res.response.data)
    })
    return () => {
      ignore = true
    }
  }, [API,projectId])

  const handleDownloadSubmissions = () => {
    // TODO: implement this!
  }

  return (
    <>
      <SubmissionsTable submissions={submissions} onChange={setSubmissions}/>
    </>
  )
}

export default SubmissionsTab
