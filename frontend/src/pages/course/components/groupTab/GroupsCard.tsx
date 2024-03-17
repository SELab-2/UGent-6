import { Card, Collapse, CollapseProps } from "antd"
import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests"
import GroupList from "./GroupList"

export type ClusterType = GET_Responses[ApiRoutes.COURSE_CLUSTERS][number]

const GroupsCard: FC<{ courseId: number }> = ({ courseId }) => {
  const [groups, setGroups] = useState<ClusterType[] | null>(null)

  useEffect(() => {
    // TODO: do the fetch (get all clusters from the course )

    setTimeout(() => {
      setGroups([
        {
          capacity: 10,
          clusterid: 1,
          course_url: "/api/courses/1",
          groups: [
            {
              group_url: "/api/groups/1",
              name: "Groep 1",
            },
            {
              group_url: "/api/groups/2",
              name: "Groep 2",
            },
            {
              group_url: "/api/groups/3",
              name: "Groep 3",
            },
          ],
          name: "Project 1 groups",
        },
        {
          capacity: 100,
          clusterid: 2,
          course_url: "/api/courses/1",
          groups: [
            {
              group_url: "/api/groups/4",
              name: "Groep 4",
            },
            {
              group_url: "/api/groups/5",
              name: "Groep 5",
            },
            {
              group_url: "/api/groups/6",
              name: "Groep 6",
            },
          ],
          name: "Project 2 groups",
        },
        {
          capacity: 120,
          clusterid: 3,
          course_url: "/api/courses/1",
          groups: [
            {
              group_url: "/api/groups/7",
              name: "Groep 7",
            },
            {
              group_url: "/api/groups/8",
              name: "Groep 8",
            },
            {
              group_url: "/api/groups/9",
              name: "Groep 9",
            },
          ],
          name: "Project 3 groups",
        },
      ])
    }, 250)
  }, [courseId])


  const items:CollapseProps['items'] = groups?.map((group) => ({

    key: group.clusterid.toString(),
    label: group.name,
    children: group.groups.map((g) => (
      <GroupList key={g.group_url} groupUrl={g.group_url} />
    )),
  })) ?? []

  return (
    <Card
     
      styles={{
        body: {
          padding: "0",
        },
      }}
    >
      <Collapse items={items} />
    </Card>
  )
}

export default GroupsCard
