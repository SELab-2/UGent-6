import React, { FC, useEffect, useState } from "react"
import { Button, Divider, Select, SelectProps, Space, Typography } from "antd"
import { ApiRoutes } from "../../../@types/requests.d"
import { PlusOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import useAppApi from "../../../hooks/useAppApi"
import GroupClusterModalContent from "./GroupClusterModalContent"
import { ClusterType } from "../../course/components/groupTab/GroupsCard"
import useApi from "../../../hooks/useApi"

type GroupClusterDropdownProps = {
  courseId: string | number
  onClusterCreated?: (clusterId: number) => void

} & SelectProps

const DropdownItem: FC<{ cluster: ClusterType; groupCountText: string; capacityText: string }> = ({ cluster, capacityText, groupCountText }) => (
  <div style={{ width: "100%", display: "flex", justifyContent: "space-between" }}>
    <Space>
      <Typography.Text>{cluster.name}</Typography.Text>
    </Space>
    <Space>
      <Typography.Text type="secondary">
        {groupCountText}: {cluster.groupCount},
      </Typography.Text>
      <Typography.Text type="secondary">
        {capacityText}: {cluster.capacity}
      </Typography.Text>
    </Space>
  </div>
)

const GroupClusterDropdown: React.FC<GroupClusterDropdownProps> = ({ courseId,onClusterCreated, ...args }) => {
  const [clusters, setClusters] = useState<SelectProps["options"] | null>(null) // Gebruik Cluster-interface
  const [loading, setLoading] = useState(false)
  const { t } = useTranslation()
  const { modal } = useAppApi()
  const API = useApi()
  const groupCountText = t("project.change.amountOfGroups")
  const capacityText = t("project.change.groupSize")

  const dropdownClusterItem = (cluster: ClusterType) => {
    return {
      label: (
        <DropdownItem
          capacityText={capacityText}
          groupCountText={groupCountText}
          cluster={cluster}
        />
      ),
      value: cluster.clusterId,
    }
  }

  useEffect(() => {
    const fetchClusters = async () => {
      setLoading(true)

      try {
        const response = await API.GET(ApiRoutes.COURSE_CLUSTERS, { pathValues: { id: courseId } })
        if (!response.success) return
        const clusters = response.response.data.map(dropdownClusterItem)
        setClusters(clusters) // Zorg ervoor dat de nieuwe staat correct wordt doorgegeven
      } catch (error) {
        console.error("Error fetching clusters:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchClusters()
  }, [courseId, t])

  const onNewCluster = () => {
    const context = modal.info({
      title: t("project.change.newGroupCluster"),
      icon: null,
      width: 500,
      content: (
        <GroupClusterModalContent
          courseId={courseId}
          onClose={() => context.destroy()}
          onClusterCreated={(c) => {
            setClusters((clusters) => [...(clusters ?? []), dropdownClusterItem(c)])
            if (onClusterCreated) onClusterCreated(c.clusterId)
            context.destroy()
          }}
        />
      ),
      footer: null,
    })
  }

  return (
    <Select
      {...args}
      loading={loading || clusters === null}
      options={clusters??[]}
      notFoundContent={t("project.change.noClusters")}
      dropdownRender={(menu) => (
        <>
          {menu}
          <Divider style={{ margin: "8px 0" }} />
          <Button
            type="text"
            style={{ width: "100%", textAlign: "left" }}
            icon={<PlusOutlined />}
            onClick={onNewCluster}
          >
            {t("project.change.newGroupCluster")}
          </Button>
        </>
      )}
    />
  )
}

export default GroupClusterDropdown
