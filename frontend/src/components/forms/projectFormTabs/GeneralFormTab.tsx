import { DatePicker, Form, FormInstance, Input, Switch, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { FC } from "react"
import MarkdownEditor from "../../input/MarkdownEditor"

const GeneralFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const description = Form.useWatch("description", form)

  return (
    <>
      <Form.Item
        label={t("project.change.name")}
        name="name"
        rules={[{ required: true, message: t("project.change.nameMessage") }]}
      >
        <Input />
      </Form.Item>

    <Typography.Text>
        {t("project.change.description")}
    </Typography.Text>
      <MarkdownEditor value={description} maxLength={5000}  />

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
        tooltip={t("project.change.maxScoreHelp")}
        rules={[{ required: false, message: t("project.change.maxScoreMessage") }]}
      >
        <Input
          min={1}
          max={1000}
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
