import { Alert, Form, Modal } from "antd"
import { FC, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import CourseForm from "../../../components/forms/CourseForm"
import { ApiRoutes } from "../../../@types/requests.d"
import useAppApi from "../../../hooks/useAppApi"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import useUser from "../../../hooks/useUser"
import useApi from "../../../hooks/useApi"

const CreateCourseModal: FC<{ open: boolean,setOpen:(b:boolean)=>void }> = ({ open,setOpen }) => {
  const { t } = useTranslation()
  const [form] = Form.useForm()
  const [error, setError] = useState<string | null>(null)
  const [loading,setLoading] = useState(false)
  const {message} = useAppApi()
  const navigate = useNavigate()
  const {updateCourses} = useUser()
  const API = useApi()

  useEffect(()=> {
    form.setFieldValue("year", new Date().getFullYear()-1)
  },[])

  const onFinish = async () => {
    await form.validateFields()
    setError(null)
    
    const values:{name:string, description:string} = form.getFieldsValue()
    console.log(values);
    values.description ??= ""
    setLoading(true)
    const res = await API.POST(ApiRoutes.COURSES, { body:values}, "message")
    if(!res.success) return setLoading(false)
    const course=  res.response
    message.success(t("home.courseCreated"))
    await updateCourses()
    navigate(AppRoutes.COURSE.replace(":courseId", course.data.courseId.toString()))
  
  }

  return (
    <Modal
      open={open}
      title={t("home.createCourse")}
      onCancel={() => setOpen(false)}
      onOk={onFinish}
      okText={t("course.createCourse")}
      okButtonProps={{ loading }}
      cancelText={t("cancel")}
      
    >
      {error && <Alert style={{marginBottom:"1rem"}} type="error" message={error} />}
     <CourseForm form={form} />
    </Modal>
  )
}

export default CreateCourseModal
