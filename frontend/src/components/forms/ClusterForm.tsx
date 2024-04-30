import { Form, Input, InputNumber } from "antd"
import { useTranslation } from "react-i18next"



const ClusterForm = () => {
    const {t} = useTranslation()


  return <>
  <Form.Item name="name" label={t("project.change.clusterName")} rules={[{ required: true }]}>
    <Input maxLength={25} />
  </Form.Item>

  <Form.Item name="groupCount" label={t("project.change.amountOfGroups")} rules={[{ required: true }, {type: 'number', min: 1, max: 500,message:t("project.change.amountOfGroupsMessage")  }]}>
    <InputNumber min={1} max={500} style={{width:"100%"}} />
  </Form.Item>

  <Form.Item name="capacity" tooltip={t("project.change.groupSizeTooltip")} label={t("project.change.groupSize")} rules={[{ required: true }, { type: 'number', min: 1, max: 500,message:t("project.change.groupSizeMessage")  }]}>
    <InputNumber min={1} max={500} style={{width:"100%"}} />
  </Form.Item>
  
  </>
}

export default ClusterForm