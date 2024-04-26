import { Button, Form, Space } from "antd"
import ClusterForm from "../../../components/forms/ClusterForm"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses, POST_Requests } from "../../../@types/requests.d"
import apiCall from "../../../util/apiFetch"
import { FC } from "react"
import { ClusterType } from "../../course/components/groupTab/GroupsCard"
import { useParams } from "react-router-dom"

const GroupClusterModalContent: FC<{ onClose: () => void; onClusterCreated: (cluster: ClusterType) => void,courseId:number|string }> = ({courseId,onClose,onClusterCreated}) => {
  const { t } = useTranslation()

  
  const createCluster = async (values: POST_Requests[ApiRoutes.COURSE_CLUSTERS]) => {
    if (!courseId) return console.error("courseId is undefined")
    const response = await apiCall.post(ApiRoutes.COURSE_CLUSTERS, values, { id: courseId+"" })
    console.log(response.data)
    onClusterCreated(response.data)
  }

  return (
    <Form
      onFinish={createCluster}
      layout="vertical"
      validateTrigger="onBlur"
    >
      <ClusterForm />

      <Form.Item style={{ marginBottom: 0, textAlign: "center" }}>
        <Space>
          <Button onClick={onClose}>{t("cancel")}</Button>
          <Button
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
