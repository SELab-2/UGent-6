import { InboxOutlined } from "@ant-design/icons"
import { Button, Form, FormInstance, Space, Upload } from "antd"
import { UploadProps } from "antd/lib"
import { FC } from "react"
import { useTranslation } from "react-i18next"


const props: UploadProps = {
  style:{ width:"100%"},
  name: "file",
  multiple: false,
  accept: ".zip",
  directory: true,
}

const SubmitForm:FC<{form:FormInstance}> = ({form}) => {

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
    <Form form={form} layout="vertical" style={{height:"100%"}}>
    
        <Form.Item
          name={t("project.addFiles")}
          valuePropName="fileList"
          getValueFromEvent={normFile}
          style={{height:"100%"}}
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
    </Form>
  )
}

export default SubmitForm
