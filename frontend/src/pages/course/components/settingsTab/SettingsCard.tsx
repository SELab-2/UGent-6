import { Button, Card, Form, Switch } from "antd"
import { FC, useEffect, useState } from "react"
import CourseForm from "../../../../components/forms/CourseForm"
import { useTranslation } from "react-i18next"
import useCourse from "../../../../hooks/useCourse"
import useAppApi from "../../../../hooks/useAppApi"
import {  SaveOutlined } from "@ant-design/icons"

const SettingsCard: FC = () => {
  const course = useCourse()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const { t } = useTranslation()
  const { message } = useAppApi()

  useEffect(() => {
    form.setFieldsValue(course)
  }, [course])

  const saveCourse = (values: any) => {
    console.log(values)

    setLoading(true)

    // TODO: do api call to update course

    setTimeout(() => {
      setLoading(false)

      message.success(t("course.changesSaved"))
    }, 1000)
  }

  return (
    <Card title={t("course.settings")} styles={{body:{display:"flex",justifyContent:"center"}}}>
      <div style={{maxWidth:"600px",width:"100%"}}>
      <CourseForm form={form} >
        <Form.Item  name="archived" label={t("course.archived")} valuePropName="checked">
          <Switch />
        </Form.Item>
      </CourseForm>
      <div style={{width:"100%",textAlign:"center"}}>
        <Button
          loading={loading}
          type="primary"
          icon={<SaveOutlined />}
          onClick={saveCourse}
        >
          {t("course.save")}
        </Button>
      </div>
      </div>
    </Card>
  )
}

export default SettingsCard