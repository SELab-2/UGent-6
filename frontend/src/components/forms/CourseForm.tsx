import { Form, FormInstance, Input } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"



const CourseForm:FC<{form:FormInstance}> = ({form}) => {
  const { t } = useTranslation()

  return (
    <Form form={form} layout="vertical">
      <Form.Item
        label={t("home.courseName")}
        name="name"
      >
        <Input
          maxLength={100}
          placeholder={t("home.courseName")}
        />
      </Form.Item>

      <Form.Item
        label={t("home.courseDescription")}
        name="description"
      >
        <Input.TextArea
          maxLength={500}
          placeholder={t("home.courseDescription")}
        />
      </Form.Item>
    </Form>
  )
}

export default CourseForm
