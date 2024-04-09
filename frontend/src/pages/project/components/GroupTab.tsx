import { Card } from "antd"
import { FC, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import GroupList from "../../course/components/groupTab/GroupList"

export type GroupType = GET_Responses[ApiRoutes.PROJECT_GROUPS][number]

const GroupTab: FC<{}> = () => {
  const [groups, setGroups] = useState<null | GroupType[]>(null)

  useEffect(() => {
    //TODO: perform get request to api/projects/{projectid}/groups
    setTimeout(() => {
      setGroups([
        {
          groupId: 1,
          name: "Groep 1",
          capacity: 10,
          groupcluster_url: "/groups/1",
          members: [
            {
              name: "ikke",
              surname: "ikee",
              userId: 1,
              url: "/users/1",
            },
            {
              name: "boby",
              surname: "babsy",
              userId: 44,
              url: "/users/44",
            },
            {
              name: "boba",
              surname: "bibs",
              userId: 42,
              url: "/users/42",
            },
            {
              name: "bobs",
              surname: "bobo",
              userId: 41,
              url: "/users/41",
            },
          ],
        },
        {
          groupId: 2,

          name: "string",
          capacity: 4,
          groupcluster_url: "api/clusters/:id",
          members: [
            {
              name: "ok",
              surname: "idk",
              userId: 43,
              url: "/users/43",
            },
            {
              name: "idk",
              surname: "k",
              userId: 44,
              url: "/users/44",
            },
            {
              name: "baaaaaaoba",
              surname: "biaaaaaabs",
              userId: 42,
              url: "/users/42",
            },
            {
              name: "aaaaaaa",
              surname: "bbbbbbbb",
              userId: 41,
              url: "/users/41",
            },
          ],
        },
      ])
    }, 350)
  }, [])

  return (
      <GroupList groups={groups} />
  )
}

export default GroupTab
