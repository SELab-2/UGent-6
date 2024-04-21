import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import GroupList from "../../course/components/groupTab/GroupList"
import apiCall from "../../../util/apiFetch"
import { useParams } from "react-router-dom"

export type GroupType = GET_Responses[ApiRoutes.PROJECT_GROUPS][number]

const GroupTab: FC<{}> = () => {
  const [groups, setGroups] = useState<null | GroupType[]>(null)
  const { projectId } = useParams()

  useEffect(() => {
    //TODO: perform get request to api/projects/{projectid}/groups

    fetchGroups()
  }, [])

  const fetchGroups = async () => {
    if (!projectId) return console.error("No projectId found")
    const res = await apiCall.get(ApiRoutes.PROJECT_GROUPS, { id: projectId })
    console.log(res.data)
    setGroups(res.data)
  }

  return (
    <GroupList
      groups={groups}
      onChanged={fetchGroups}
    />
  )
}

export default GroupTab
