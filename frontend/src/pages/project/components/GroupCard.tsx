import { Card } from "antd"
import { FC, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import GroupList from "../../course/components/groupTab/GroupList"

export type GroupType = GET_Responses[ApiRoutes.PROJECT_GROUPS][number]

const GroupCard: FC<{}> = () => {
  const { t } = useTranslation()

  const [groups, setGroups] = useState<null | GroupType[]>(null)

  useEffect(() => {
    //TODO: perform get request to api/projects/{projectid}/groups
    setTimeout(() => {
      setGroups([
        {
          groupid: 1,
          name: "Groep 1",
          capacity: 10,
          groupcluster_url: "/groups/1",
          members: [
            {
              name: "ikke",
              surname: "ikee",
              userid: 1,
              url: "/users/1",
            },
            {
              name: "boby",
              surname: "babsy",
              userid: 44,
              url: "/users/44",
            },
            {
              name: "boba",
              surname: "bibs",
              userid: 42,
              url: "/users/42",
            },
            {
              name: "bobs",
              surname: "bobo",
              userid: 41,
              url: "/users/41",
            },
          ],
        },
        {
          groupid: 2,

          name: "string",
          capacity: 4,
          groupcluster_url: "api/clusters/:id",
          members: [
            {
              name: "ok",
              surname: "idk",
              userid: 43,
              url: "/users/43",
            },
            {
              name: "idk",
              surname: "k",
              userid: 44,
              url: "/users/44",
            },
            {
              name: "baaaaaaoba",
              surname: "biaaaaaabs",
              userid: 42,
              url: "/users/42",
            },
            {
              name: "aaaaaaa",
              surname: "bbbbbbbb",
              userid: 41,
              url: "/users/41",
            },
          ],
        },
      ])
    }, 350)
  }, [])

  return (
    <Card title={t("course.groups")} styles={{body:{
      padding: 16
    }}}>
      <GroupList groups={groups} />
    </Card>
  )
}

export default GroupCard
