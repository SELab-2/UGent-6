import { Card, Collapse, CollapseProps, Spin } from "antd"
import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests"
import GroupList from "./GroupList"
import { CardProps } from "antd/lib"
import GroupCollapseItem from "./GroupCollapseItem"

export type ClusterType = GET_Responses[ApiRoutes.COURSE_CLUSTERS][number]

const GroupsCard: FC<{ courseId: number | null; cardProps?: CardProps }> = ({ courseId, cardProps }) => {
  const [groups, setGroups] = useState<ClusterType[] | null>(null)

  useEffect(() => {
    // TODO: do the fetch (get all clusters from the course )
    if (!courseId) return // if course is null that means it hasn't been fetched yet by the parent component
    setTimeout(() => {
      setGroups([
        {
          capacity: 10,
          clusterId: 1,
          course_url: "/api/courses/1",
          groups: [
            {
              group_url: "/api/groups/1",
              name: "Groep 1",
              groupId: 1
            },
            {
              group_url: "/api/groups/2",
              name: "Groep 2",
              groupId: 2
            },
            {
              group_url: "/api/groups/3",
              name: "Groep 3",
              groupId: 3
            },
          ],
          created_at: "2022-12-21T16:00:00.000000Z",
          groupCount: 3,
          name: "Project 1 groups",
        },
        {
          capacity: 100,
          clusterId: 2,
          course_url: "/api/courses/2",
          groups: [
            {
              group_url: "/api/groups/4",
              name: "Groep 4",
              groupId: 4
            },
            {
              group_url: "/api/groups/5",
              name: "Groep 5",
              groupId: 5
            },
            {
              group_url: "/api/groups/6",
              name: "Groep 6",
              groupId: 6
            },
          ],
          created_at: "2022-12-21T16:00:00.000000Z",
          groupCount: 3,
          name: "Project 2 groups",
        },
        {
          capacity: 120,
          clusterId: 3,
          course_url: "/api/courses/3",
          groups: [
            {
              group_url: "/api/groups/7",
              name: "Groep 7",
              groupId: 7
            },
            {
              group_url: "/api/groups/8",
              name: "Groep 8",
              groupId: 8
            },
            {
              group_url: "/api/groups/9",
              name: "Groep 9",
              groupId: 9
            },
          ],
          created_at: "2022-12-21T16:00:00.000000Z",
          groupCount: 3,
          name: "Project 3 groups",
        },
      ])
    }, 250)
  }, [courseId])

  // if(!groups) return <div style={{width:"100%",height:"400px",display:"flex",justifyContent:"center",alignItems:"center"}}>
  //   <Spin tip="Loading"></Spin>
  // </div>

  const items: CollapseProps["items"] =
    groups?.map((group) => ({
      key: group.clusterId.toString(),
      label: group.name,
      children: group.groups.map((g) => (
        <GroupCollapseItem
          key={g.group_url}
          groupUrl={g.group_url}
        />
      )),
    })) ?? []

  return (
    <Card
      {...cardProps}
      loading={!groups}
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
