import { Alert, Form, Modal } from "antd"
import { FC, useState } from "react"
import { useTranslation } from "react-i18next"
import CourseForm from "../../../components/forms/CourseForm"
import apiCall, { POST_Error } from "../../../util/apiFetch"
import { ApiRoutes } from "../../../@types/requests.d"
import useAppApi from "../../../hooks/useAppApi"
import axios, { AxiosError } from "axios"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import useUser from "../../../hooks/useUser"

const CreateCourseModal: FC<{ open: boolean,setOpen:(b:boolean)=>void }> = ({ open,setOpen }) => {
  const { t } = useTranslation()
  const [form] = Form.useForm()
  const [error, setError] = useState<string | null>(null)
  const [loading,setLoading] = useState(false)
  const {message} = useAppApi()
  const navigate = useNavigate()
  const {updateCourses} = useUser()


  const onFinish = async () => {
    await form.validateFields()
    setError(null)
    
    const values:{name:string, description:string} = form.getFieldsValue()
    console.log(values);
    values.description ??= ""
    setLoading(true)
    try {
      const course =  await apiCall.post(ApiRoutes.COURSES, values)
      message.success(t("home.courseCreated"))
      await updateCourses()
      navigate(AppRoutes.COURSE.replace(":courseId", course.data.courseId.toString()))
    } catch(err){
      console.error(err);
      if(axios.isAxiosError(err)){
        setError(err.response?.data.message || t("woops"))
      } else {
        message.error(t("woops"))
      }
    } finally {
      setLoading(false)
    }
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
