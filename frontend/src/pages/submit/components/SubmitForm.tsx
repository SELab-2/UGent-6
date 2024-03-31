import { InboxOutlined } from "@ant-design/icons"
import { Button, Form, Space, Upload } from "antd"
import { UploadProps } from "antd/lib"
import { useTranslation } from "react-i18next"


const props: UploadProps = {
  style:{ width:"100%"},
  name: "file",
  multiple: false,
  accept: ".zip",
  directory: true,
}

const SubmitForm = () => {
  const [form] = Form.useForm()

  const {t} = useTranslation()
  const normFile = (e: any) => {
    console.log("Upload event:", e)
    if (Array.isArray(e)) {
      return e
    }
    return e?.fileList
  }

  const onFinish = (values: any) => {
    console.log("Received values of form: ", values)

    // TODO: make api call
  }

  return (
    <Form form={form} layout="vertical">
    
        <Form.Item
          name={t("project.addFiles")}
          valuePropName="fileList"
          getValueFromEvent={normFile}
          noStyle
        >
          <Upload.Dragger
            {...props}
            style={{ height:"100%"}}
          >
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">{t("project.uploadAreaTitle")}</p>
            <p className="ant-upload-hint">{t("project.uploadAreaSubtitle")}</p>
          </Upload.Dragger>
        </Form.Item>

      <Form.Item  style={{width:"100%",textAlign:"center",marginTop:"2rem"}}>
          <Button
            type="primary"
            size="large"
            htmlType="submit"
            disabled={!form.getFieldValue("fileList")?.length}
          >
            {t("project.submit")}
          </Button>
      </Form.Item>
    </Form>
  )
}

export default SubmitForm
