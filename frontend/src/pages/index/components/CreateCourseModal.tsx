import { Alert, Form } from "antd"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import CourseForm from "../../../components/forms/CourseForm"
import { ApiRoutes } from "../../../@types/requests.d"
import useAppApi from "../../../hooks/useAppApi"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import useUser from "../../../hooks/useUser"
import useApi from "../../../hooks/useApi"

const createCourseModal = () => {
  const { t } = useTranslation()
  const [form] = Form.useForm()
  const [error, setError] = useState<string | null>(null)
  const { message, modal } = useAppApi()
  const navigate = useNavigate()
  const { updateCourses } = useUser()
  const API = useApi()

  useEffect(() => {
    form.setFieldValue("year", new Date().getFullYear() - 1)
  }, [])

  const onFinish = () => {
    return new Promise<void>(async (resolve, reject) => {
      await form.validateFields()
      setError(null)

      const values: { name: string; description: string } = form.getFieldsValue()
      console.log(values)
      values.description ??= ""
      const res = await API.POST(ApiRoutes.COURSES, { body: values }, "message")
      if (!res.success) return reject()
      const course = res.response
      message.success(t("home.courseCreated"))
      await updateCourses()
      navigate(AppRoutes.COURSE.replace(":courseId", course.data.courseId.toString()))
      resolve()
    })
  }

  return {
    showModal: () => {
      modal.info({
        title: t("home.createCourse"),
        width: 500,
        className: "modal-no-icon",
        onOk: onFinish,
        okText: t("course.createCourse"),
        cancelText: t("cancel"),
        okCancel: true,
        icon: null,
        content: (
          <>
            {error && (
              <Alert
                style={{ marginBottom: "1rem" }}
                type="error"
                message={error}
              />
            )}
            <CourseForm form={form} />
          </>
        ),
      })
    },
  }
}

export default createCourseModal
