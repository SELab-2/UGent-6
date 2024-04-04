import { FC, useEffect, useState } from "react"
import GroupList, { GroupType } from "./GroupList"

const GroupCollapseItem: FC<{ clustedId: number }> = ({ clustedId }) => {
  const [group, setGroup] = useState<GroupType[] | null>(null)

  useEffect(() => {
    // TODO: make request to `groupUrl`

    setTimeout(() => {
      setGroup([
        {
          capacity: 10,
          groupcluster_url: "/api/clusters/1",
          groupId: 1,
          name: "Groep 1",
          members: [
            {
              url: "/api/groups/1/members/1",
              name: "Piet5",
              surname: "Jansen",
              userId: 1,
            },
            {
              url: "/api/groups/1/members/2",
              name: "Klaas5",
              surname: "Jansen",
              userId: 2,
            },
            {
              url: "/api/groups/1/members/3",
              name: "Jan4",
              surname: "Jansen",
              userId: 3,
            },
            {
              url: "/api/groups/1/members/4",
              name: "Bart4",
              surname: "Jansen",
              userId: 4,
            },
          ],
        },

        {
          capacity: 12,
          groupcluster_url: "/api/clusters/2",
          groupId: 2,
          name: "Groep 2",
          members: [
            {
              url: "/api/groups/2/members/11",
              name: "Piet2",
              surname: "Jansen",
              userId: 11,
            },
            {
              url: "/api/groups/2/members/12",
              name: "Klaas2",
              surname: "Jansen",
              userId: 12,
            },
            {
              url: "/api/groups/2/members/13",
              name: "Jan3",
              surname: "Jansen",
              userId: 13,
            },
            {
              url: "/api/groups/2/members/14",
              name: "Bart3",
              surname: "Jansen",
              userId: 14,
            },
          ],
        },
      ])
    }, 250)
  }, [])



  return <GroupList groups={group} />
}

export default GroupCollapseItem
