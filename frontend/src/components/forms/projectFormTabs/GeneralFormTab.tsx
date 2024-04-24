import { DatePicker, Form, Input, Switch } from "antd"
import { useTranslation } from "react-i18next"
import GroupClusterDropdown from "../../../pages/projectCreate/components/GroupClusterDropdown"
import { useParams } from "react-router-dom"
import { FC } from "react"
import MarkdownEditor from "../../input/MarkdownEditor"

const GeneralFormTab:FC = () => {
  const { t } = useTranslation()

  return (
    <>
      <Form.Item
        label={t("project.change.name")}
        name="name"
        rules={[{ required: true, message: t("project.change.nameMessage") }]}
      >
        <Input />
      </Form.Item>
      <Form.Item
        label={t("project.change.description")}
        name="description"
        rules={[{ required: true, message: t("project.change.descriptionMessage") }]}
      >
        <MarkdownEditor/>
      </Form.Item>
      
      <Form.Item
        label={t("project.change.visible")}
        required
        name="visible"
        valuePropName="checked"
      >
        <Switch />
      </Form.Item>
      <Form.Item
        label={t("project.change.maxScore")}
        name="maxScore"
        rules={[{ required: true, message: t("project.change.maxScoreMessage") }]}
      >
        <Input
          min={1}
          type="number"
        />
      </Form.Item>
      <Form.Item
        label={t("project.change.deadline")}
        name="deadline"
        rules={[{ required: true }]}
      >
        <DatePicker
          showTime
          format="YYYY-MM-DD HH:mm:ss"
        />
      </Form.Item>
    </>
  )
}

export default GeneralFormTab
