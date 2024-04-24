import { Form } from "antd"
import GroupClusterDropdown from "../../../pages/projectCreate/components/GroupClusterDropdown"
import { useParams } from "react-router-dom"
import { useTranslation } from "react-i18next"




const GroupsFormTab  = () => {
 
  const { courseId } = useParams<{ courseId: string }>()
  const { t } = useTranslation()



  return <>
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
  </>
}

export default GroupsFormTab