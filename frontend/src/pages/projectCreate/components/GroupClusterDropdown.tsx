import React, { useEffect, useState } from "react"
import { Button, Divider, Select, SelectProps, Space, Typography } from "antd"
import { ApiRoutes } from "../../../@types/requests.d"
import apiCall from "../../../util/apiFetch"
import { PlusOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import useAppApi from "../../../hooks/useAppApi"
import GroupClusterModalContent from "./GroupClusterModalContent"
import { ClusterType } from "../../course/components/groupTab/GroupsCard"

interface Cluster {
  clusterId: number
  name: string
  capacity: number
  groupCount: number
  groups: {
    groupId: number
    capacity: number
    name: string
    groupClusterUrl: ApiRoutes.CLUSTER
    members: any[] // Aanpas dit aan het werkelijke type van members
  }[]
  courseUrl: ApiRoutes.COURSE
}

interface GroupClusterDropdownProps {
  courseId: string | number
}

const GroupClusterDropdown: React.FC<GroupClusterDropdownProps & SelectProps> = ({ courseId, ...args }) => {
  const [clusters, setClusters] = useState<SelectProps["options"]>([]) // Gebruik Cluster-interface
  const [selectedCluster, setSelectedCluster] = useState<ClusterType | null>(null)
  const [loading, setLoading] = useState(false)
  const { t } = useTranslation()
  const { modal } = useAppApi()

  useEffect(() => {
    const fetchClusters = async () => {
      setLoading(true)
      try {
        const response = await apiCall.get(ApiRoutes.COURSE_CLUSTERS, { id: courseId })
        const options: SelectProps["options"] = response.data.map((cluster: Cluster) => ({ label: cluster.name, value: cluster.clusterId }))

        setClusters(options) // Zorg ervoor dat de nieuwe staat correct wordt doorgegeven
      } catch (error) {
        console.error("Error fetching clusters:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchClusters()
  }, [courseId])

  const onNewCluster = () => {
    const context = modal.info({
      title: t("project.change.newGroupCluster"),
      icon: null,
      content: (
        <GroupClusterModalContent
          courseId={courseId}
          onClose={() => context.destroy()}
          onClusterCreated={(c) => {
            setSelectedCluster(c)
            const option = { label: c.name, value: c.clusterId }
            setClusters((cl) => [...cl!, option])
            if(args.onChange) args.onChange(c.clusterId, option)
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
      loading={loading}
      options={clusters}
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
