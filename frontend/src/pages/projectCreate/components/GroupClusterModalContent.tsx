import { Button, Form, Space } from "antd"
import ClusterForm from "../../../components/forms/ClusterForm"
import { useTranslation } from "react-i18next"
import { ApiRoutes, POST_Requests } from "../../../@types/requests.d"
import { FC, useState } from "react"
import { ClusterType } from "../../course/components/groupTab/GroupsCard"
import useApi from "../../../hooks/useApi"

const GroupClusterModalContent: FC<{ onClose: () => void; onClusterCreated: (cluster: ClusterType) => void; courseId: number | string }> = ({ courseId, onClose, onClusterCreated }) => {
  const { t } = useTranslation()
  const [loading, setLoading] = useState<boolean>(false)
  const API = useApi()

  const createCluster = async (values: POST_Requests[ApiRoutes.COURSE_CLUSTERS]) => {
    if (!courseId) return console.error("courseId is undefined")
    setLoading(true)
    const response = await API.POST(
      ApiRoutes.COURSE_CLUSTERS,
      { body: values, pathValues: { id: courseId.toString() } },
      {
        mode: "message",
        successMessage: t("project.change.groupClusterCreated"),
      }
    )
    setLoading(false)
    if (!response.success) return

    onClusterCreated(response.response.data)
  }

  return (
    <Form
      onFinish={createCluster}
      layout="vertical"
      validateTrigger="onBlur"
      requiredMark="optional"
    >
      <ClusterForm />

      <Form.Item style={{ marginBottom: 0, textAlign: "center" }}>
        <Space>
          <Button onClick={onClose}>{t("cancel")}</Button>
          <Button
            loading={loading}
            type="primary"
            htmlType="submit"
          >
            {t("project.change.makeCluster")}
          </Button>
        </Space>
      </Form.Item>
    </Form>
  )
}

export default GroupClusterModalContent
