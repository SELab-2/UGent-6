import { Form, FormInstance, Input, Typography } from "antd"
import { FC, PropsWithChildren } from "react"
import { useTranslation } from "react-i18next"
import MarkdownEditor from "../input/MarkdownEditor"



const CourseForm:FC<{form:FormInstance} & PropsWithChildren> = ({form,children}) => {
  const { t } = useTranslation()
  const description = Form.useWatch("description", form)

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

      <Typography.Text>
        {t("project.change.description")}
    </Typography.Text>
        <MarkdownEditor
          maxLength={5000}
          value={description}
          placeholder={t("home.courseDescription")}
          
        />
      {children}
    </Form>
  )
}

export default CourseForm
