import { FC, useEffect, useState } from "react"
import GroupList, { GroupType } from "./GroupList"

const GroupCollapseItem: FC<{ groupUrl: string }> = ({ groupUrl }) => {
  const [group, setGroup] = useState<GroupType[] | null>(null)

  useEffect(() => {
    // TODO: make request to `groupUrl`

    setTimeout(() => {
      setGroup([
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
            {
              url: "/api/groups/1/members/3",
              name: "Jan",
              surname: "Jansen",
              userid: 3,
            },
            {
              url: "/api/groups/1/members/4",
              name: "Bart",
              surname: "Jansen",
              userid: 4,
            },
          ],
        },

        {
          capacity: 12,
          groupcluster_url: "/api/clusters/2",
          groupid: 2,
          name: "Groep 2",
          members: [
            {
              url: "/api/groups/2/members/11",
              name: "Piet",
              surname: "Jansen",
              userid: 11,
            },
            {
              url: "/api/groups/2/members/12",
              name: "Klaas",
              surname: "Jansen",
              userid: 12,
            },
            {
              url: "/api/groups/2/members/13",
              name: "Jan",
              surname: "Jansen",
              userid: 13,
            },
            {
              url: "/api/groups/2/members/14",
              name: "Bart",
              surname: "Jansen",
              userid: 14,
            },
          ],
        },
      ])
    }, 250)
  }, [])

  return <GroupList groups={group} />
}

export default GroupCollapseItem
