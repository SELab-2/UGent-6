import { Form } from "antd"
import GroupClusterDropdown from "../../../pages/projectCreate/components/GroupClusterDropdown"
import { useParams } from "react-router-dom"
import { useTranslation } from "react-i18next"
import { FC, useEffect, useState } from "react"
import { FormInstance } from "antd/lib"
import apiCall from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"
import { ClusterType } from "../../../pages/course/components/groupTab/GroupsCard"
import { Spin } from "antd"
import GroupList from "../../../pages/course/components/groupTab/GroupList"
import GroupMembersTransfer from "../../other/GroupMembersTransfer"

const GroupsFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { courseId } = useParams<{ courseId: string }>()
  const { t } = useTranslation()
  const [selectedCluster, setSelectedCluster] = useState<ClusterType | null>(null)

  const selectedClusterId = Form.useWatch("groupClusterId", form)

  console.log(selectedClusterId)

  useEffect(() => {
    if (selectedClusterId == null) setSelectedCluster(null)
    else {
      fetchCluster()
    }
  }, [selectedClusterId])

  const fetchCluster = async () => {
    const response = await apiCall.get(ApiRoutes.CLUSTER, { id: selectedClusterId })
    setSelectedCluster(response.data)
  }

  return (
    <>
      <Form.Item
        label={t("project.change.groupClusterId")}
        name="groupClusterId"
        tooltip={t("project.change.groupClusterIdTooltip")}
      >
        <GroupClusterDropdown
          allowClear
          courseId={courseId!}
        />
      </Form.Item>

      {selectedClusterId != null && courseId && (
        <>
          {selectedCluster ? (
            <>
              <GroupMembersTransfer
                groups={selectedCluster.groups}
                onChanged={fetchCluster}
                courseId={courseId}
              />
            </>
          ) : (
            <Spin />
          )}
        </>
      )}
    </>
  )
}

export default GroupsFormTab
