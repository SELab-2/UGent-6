import { Checkbox, DatePicker, Form, FormInstance, Input, Switch } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import GroupClusterDropdown from "../../pages/projectCreate/components/GroupClusterDropdown"
import { useParams } from "react-router-dom"

const ProjectForm: FC<{}> = () => {
  const { t } = useTranslation()
  const { courseId } = useParams<{ courseId: string }>()

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
        <Input.TextArea />
      </Form.Item>
      <Form.Item
        label={t("project.change.groupClusterId")}
        name="groupClusterId"
        rules={[{ required: true, message: t("project.change.groupClusterIdMessage") }]}
      >
        <GroupClusterDropdown courseId={courseId!} />
      </Form.Item>
      <Form.Item
        label={t("project.change.testId")}
        name="testId"
      >
        <Input type="number" />
      </Form.Item>
      <Form.Item
        label={t("project.change.visible")}
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
        <Input type="number" />
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

export default ProjectForm
