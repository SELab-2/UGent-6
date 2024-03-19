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
  }

  return (
    <Form layout="vertical">
      <Form.Item
        label={t("project.addFiles")}
        style={{ maxWidth: 800 }}
        
      >
        <Form.Item
          name={t("project.addFiles")}
          valuePropName="fileList"
          getValueFromEvent={normFile}
          noStyle
        >
          <Upload.Dragger
            {...props}
            
          >
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">{t("project.uploadAreaTitle")}</p>
            <p className="ant-upload-hint">{t("project.uploadAreaSubtitle")}</p>
          </Upload.Dragger>
        </Form.Item>
      </Form.Item>

      <Form.Item wrapperCol={{ span: 12, offset: 6 }}>
        <Space>
          <Button
            type="primary"
            htmlType="submit"
          >
            {t("project.submit")}
          </Button>
        </Space>
      </Form.Item>
    </Form>
  )
}

export default SubmitForm
