import { Form, Input, Modal } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"

const CreateCourseModal: FC<{ open: boolean,setOpen:(b:boolean)=>void }> = ({ open,setOpen }) => {
  const { t } = useTranslation()


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
      <Form layout={"vertical"}>
        <Form.Item
          label={t("home.courseName")}
          name="name"
        >
          <Input
            maxLength={100}
            placeholder={t("home.courseName")}
          />
        </Form.Item>

        <Form.Item label={t("home.courseDescription")} name="description">
          <Input.TextArea
            maxLength={500}
            placeholder={t("home.courseDescription")}
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default CreateCourseModal
