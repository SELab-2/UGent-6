import { InboxOutlined } from "@ant-design/icons"
import { Button, Form, Input, Upload } from "antd"
import { TextAreaProps } from "antd/es/input"
import { FormInstance } from "antd/lib"
import { FC } from "react"
import { useTranslation } from "react-i18next"

const UploadBtn: React.FC<{ form: FormInstance; fieldName: string; textFieldProps?: TextAreaProps }> = ({ form, fieldName, textFieldProps }) => {
  const handleFileUpload = (file: File) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const contents = e.target?.result as string
      console.log(contents)
      form.setFieldValue(fieldName, contents)
    }
    reader.readAsText(file)
    // Prevent default upload action
    return false
  }

  return (
    <>
      <div style={{ marginTop: "8px", display: "flex", justifyContent: "flex-end" }}>
        <Upload
          showUploadList={false}
          beforeUpload={handleFileUpload}
        >
          <Button icon={<InboxOutlined />}>Upload</Button>
        </Upload>
      </div>
    </>
  )
}

const DockerFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const dockerImage = Form.useWatch("dockerImage", form)

  return (
    <>
      <Form.Item
        label="Docker image"
        name="dockerImage"
        tooltip="TODO write docs for this"

      >
        <Input
          style={{ marginTop: "8px" }}
          placeholder={t("project.tests.dockerImagePlaceholder")}
        />
      </Form.Item>

      {!!dockerImage?.length && (
        <>
          <Form.Item
            rules={[{ required: true, message: "Docker script is required" }]}
            label="Docker script"
            name="dockerScript"
            tooltip="TODO write docs for this"
          >
            <Input.TextArea
              autoSize={{ minRows: 3 }}
              style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto" }}
            />
          </Form.Item>
          <UploadBtn
            form={form}
            fieldName="dockerScript"
          />

          <Form.Item
            label="Sjabloon"
            name="sjabloon"
            tooltip="TODO write docs for this"
          >
            <Input.TextArea
              autoSize={{ minRows: 3 }}
              style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto" }}
            />
          </Form.Item>
          <UploadBtn
            form={form}
            fieldName="sjabloon"
          />
        </>
      )}
    </>
  )
}

export default DockerFormTab
