import { Button, Card, Collapse, CollapseProps, Spin, Typography } from "antd"
import { FC, useEffect, useMemo, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import GroupList from "./GroupList"
import { CardProps } from "antd/lib"
import { useTranslation } from "react-i18next"
import useApi from "../../../../hooks/useApi"
import { PlusOutlined } from "@ant-design/icons"

export type ClusterType = GET_Responses[ApiRoutes.COURSE_CLUSTERS][number]

const GroupsCard: FC<{ courseId: number | null; cardProps?: CardProps }> = ({ courseId, cardProps }) => {
  const [groups, setGroups] = useState<ClusterType[] | null>(null)
  const { t } = useTranslation()
  const API = useApi()
  useEffect(() => {
    fetchGroups().catch(console.error)
  }, [courseId])

  const fetchGroups = async () => {
    if (!courseId) return // if course is null that means it hasn't been fetched yet by the parent component
    const res = await API.GET(ApiRoutes.COURSE_CLUSTERS, { pathValues: { id: courseId } })
    if (!res.success) return
    setGroups(res.response.data)
  }



  const items: CollapseProps["items"] = useMemo(
    () =>
      groups?.map((cluster) => ({
        key: cluster.clusterId.toString(),
        label: cluster.name,
        children: (
            <GroupList
              onChanged={fetchGroups}
              groups={cluster.groups}
              locked={cluster.lockGroupsAfter}
              clusterId={cluster.clusterId}
            />
            
        ),
      })),
    [groups]
  )

  if (Array.isArray(items) && !items.length)
    return (
      <div style={{ textAlign: "center" }}>
        <Typography.Text type="secondary">{t("course.noGroups")}</Typography.Text>
      </div>
    )

  if (!items)
    return (
      <div style={{ width: "100%", height: "400px", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin />
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
