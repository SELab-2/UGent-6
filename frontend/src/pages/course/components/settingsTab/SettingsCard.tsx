import { Button, Card, Form, Popconfirm, Space, Switch } from "antd"
import { FC, useContext, useEffect, useState } from "react"
import CourseForm from "../../../../components/forms/CourseForm"
import { useTranslation } from "react-i18next"
import useCourse from "../../../../hooks/useCourse"
import useAppApi from "../../../../hooks/useAppApi"
import { DeleteOutlined, SaveOutlined } from "@ant-design/icons"
import apiCall from "../../../../util/apiFetch"
import { ApiRoutes } from "../../../../@types/requests.d"
import useUser from "../../../../hooks/useUser"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../../@types/routes"
import { CourseContext } from "../../../../router/CourseRoutes"

const SettingsCard: FC = () => {
  const course = useCourse()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const { t } = useTranslation()
  const { message } = useAppApi()
  const { updateCourses } = useUser()
  const navigate = useNavigate()
  const {setCourse} = useContext(CourseContext)

  useEffect(() => {
    form.setFieldsValue(course)
    form.setFieldValue("isArchived", !!course.archivedAt)
  }, [course])

  const saveCourse = async () => {

    await form.validateFields()
    
    const values:{name:string, description:string} = form.getFieldsValue()
    console.log(values);
    values.description ??= ""
    setLoading(true)
    try {
      const res = await apiCall.patch(ApiRoutes.COURSE, values, { courseId: course.courseId })
      message.success(t("course.changesSaved"))
      setCourse(res.data)
      await updateCourses()
    } catch(err){
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const deleteCourse = async () => {
    setLoading(true)
    try {
      await apiCall.delete(ApiRoutes.COURSE, undefined, { courseId: course.courseId })
      message.success(t("course.courseDeleted"))
      await updateCourses()
      setLoading(false)
      navigate(AppRoutes.HOME)
    } catch (err) {
      console.log(err)
      //TODO: handle error
      setLoading(false)
    }
  }

  return (
    <Card
      title={t("course.settings")}
      styles={{ body: { display: "flex", justifyContent: "center" } }}
    >
      <div style={{ maxWidth: "600px", width: "100%" }}>
        <CourseForm form={form}>
          <Form.Item
            name="isArchived"
            label={t("course.archived")}
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>
        </CourseForm>
        <Space
          style={{ width: "100%", textAlign: "center" }}
          align="center"
        >
          <Button
            loading={loading}
            type="primary"
            icon={<SaveOutlined />}
            onClick={saveCourse}
          >
            {t("course.save")}
          </Button>

          <Popconfirm
            title={t("course.deleteCourse")}
            description={t("course.deleteCourseDescription")}
            onConfirm={deleteCourse}
            okText={t("course.confirmDelete")}
            okButtonProps={{ danger: true }}
            cancelText={t("course.cancel")}
            style={{ maxWidth: "100px" }}
          >
            <Button
              danger
              icon={<DeleteOutlined />}
            >
              {t("course.deleteCourse")}
            </Button>
          </Popconfirm>
        </Space>
      </div>
    </Card>
  )
}

export default SettingsCard
