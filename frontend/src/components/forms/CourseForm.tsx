import { Form, FormInstance, Input } from "antd"
import { FC, PropsWithChildren } from "react"
import { useTranslation } from "react-i18next"
import MarkdownEditor from "../input/MarkdownEditor"



const CourseForm:FC<{form:FormInstance} & PropsWithChildren> = ({form,children}) => {
  const { t } = useTranslation()

  return (
    <Form form={form} layout="vertical" validateTrigger="onBlur">
      <Form.Item
        rules={[{ required: true, message: t("home.courseNameRequired") }, { max: 50, message: t("home.courseNameMaxLength") },{ min: 3, message: t("home.courseNameMinLength") }]}
        label={t("home.courseName")}
        name="name"
      >
        <Input
          maxLength={100}
          placeholder={t("home.courseName")}
        />
      </Form.Item>

      <Form.Item
        rules={[{ max: 2000, message: t("home.courseDescriptionMaxLength") }]}
        label={t("home.courseDescription")}
        name="description"
      >
        <MarkdownEditor
          maxLength={2000}
          placeholder={t("home.courseDescription")}
          
        />
      </Form.Item>
      {children}
    </Form>
  )
}

export default CourseForm
