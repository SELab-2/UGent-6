import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import GroupList from "../../course/components/groupTab/GroupList"
import { useParams } from "react-router-dom"
import useApi from "../../../hooks/useApi"
import useProject from "../../../hooks/useProject"

export type GroupType = GET_Responses[ApiRoutes.PROJECT_GROUPS][number]

const GroupTab: FC<{}> = () => {
  const [groups, setGroups] = useState<null | GroupType[]>(null)
  const { projectId } = useParams()
  const project = useProject()
  const API = useApi()

  useEffect(() => {
    fetchGroups()
  }, [])

  const fetchGroups = async () => {
    if (!projectId) return console.error("No projectId found")
    const res = await API.GET(ApiRoutes.PROJECT_GROUPS, { pathValues: { id: projectId } })
    if (!res.success) return
    console.log(res.response.data)
    setGroups(res.response.data)
  }

  return (
    <GroupList
      groups={groups}
      onChanged={fetchGroups}
      project={project}
    />
  )
}

export default GroupTab
