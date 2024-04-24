import { Card, Collapse, CollapseProps, Spin, Typography } from "antd"
import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import GroupList from "./GroupList"
import { CardProps } from "antd/lib"
import apiCall from "../../../../util/apiFetch"
import { useTranslation } from "react-i18next"

export type ClusterType = GET_Responses[ApiRoutes.COURSE_CLUSTERS][number]

const GroupsCard: FC<{ courseId: number | null; cardProps?: CardProps }> = ({ courseId, cardProps }) => {
  const [groups, setGroups] = useState<ClusterType[] | null>(null)
  const { t } = useTranslation()
  useEffect(() => {
    // TODO: do the fetch (get all clusters from the course )
      fetchGroups().catch(console.error)

  }, [courseId])

  const fetchGroups = async () => {
    if (!courseId) return // if course is null that means it hasn't been fetched yet by the parent component
      const res = await apiCall.get(ApiRoutes.COURSE_CLUSTERS, { id: courseId })
      setGroups(res.data)
    }

  // if(!groups) return <div style={{width:"100%",height:"400px",display:"flex",justifyContent:"center",alignItems:"center"}}>
  //   <Spin tip="Loading"></Spin>
  // </div>

  const items: CollapseProps["items"] = groups?.map((cluster) => ({
    key: cluster.clusterId.toString(),
    label: cluster.name,
    children: (
      <GroupList
        onChanged={fetchGroups}
        groups={cluster.groups}
      />
    ),
  }))

  if (Array.isArray(items) && !items.length)
    return (
      <div style={{ textAlign: "center" }}>
        <Typography.Text type="secondary">{t("course.noGroups")}</Typography.Text>
      </div>
    )

  if (!items)
    return (
      <div style={{ width: "100%", height: "400px", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin tip="Loading">
          <span></span>
        </Spin>
      </div>
    )
  return (
    <Card
      {...cardProps}
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
