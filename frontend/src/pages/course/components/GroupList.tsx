import { Button, List, Typography } from "antd"
import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"

export type GroupMembers = GET_Responses[ApiRoutes.CLUSTER_GROUPS][number]

const GroupList: FC<{ groupUrl: string }> = ({ groupUrl }) => {
  const [group, setGroups] = useState<GroupMembers[] | null>(null)

  useEffect(() => {
    // TODO: do api call

    setTimeout(() => {
      setGroups([
        {
          capacity: 10,
          groupcluster_url: "/api/clusters/1",
          groupid: 1,
          name: "Groep 1",
          members: [
            {
              url: "/api/groups/1/members/1",
              name: "Piet",
              surname: "Jansen",
              userid: 1,
            },
            {
              url: "/api/groups/1/members/2",
              name: "Klaas",
              surname: "Jansen",
              userid: 2,
            },
          ],
        },
      ])
    }, 250)
  }, [groupUrl])

  const Group = ({ group }: { group: GroupMembers }) => {
    return (
      <List.Item actions={[<Button disabled={group.members.length === group.capacity}>Join</Button>]}>
        <List.Item.Meta
          title={group.name}
          description={group.members.map((m) => `${m.name} ${m.surname}`).join(", ")}
        />
      </List.Item>
    )
  }

  return (
    <List
    locale={{
      emptyText: "No groups available",
    }}
      dataSource={group ?? []}
      renderItem={(g) => <Group group={g} />}
    />
  )
}

export default GroupList
