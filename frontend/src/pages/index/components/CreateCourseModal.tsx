import { Form, Modal } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import CourseForm from "../../../components/forms/CourseForm"

const CreateCourseModal: FC<{ open: boolean,setOpen:(b:boolean)=>void }> = ({ open,setOpen }) => {
  const { t } = useTranslation()
  const [form] = Form.useForm()


  const onFinish = (values: any) => {


  }

  return (
    <Modal
      open={open}
      title={t("home.createCourse")}
      onCancel={() => setOpen(false)}
      onOk={onFinish}
      okText={t("ok")}
      cancelText={t("cancel")}
      
    >
     <CourseForm form={form} />
    </Modal>
  )
}

export default CreateCourseModal
