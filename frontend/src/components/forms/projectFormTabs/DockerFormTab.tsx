import { InboxOutlined, UploadOutlined } from "@ant-design/icons"
import { Button, Form, Input, Upload } from "antd"
import { TextAreaProps } from "antd/es/input"
import { FormInstance } from "antd/lib"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes } from "../../../@types/requests"
import useAppApi from "../../../hooks/useAppApi"

const UploadBtn: React.FC<{ form: FormInstance; fieldName: string; textFieldProps?: TextAreaProps; disabled?: boolean }> = ({ form, fieldName, disabled }) => {
  const handleFileUpload = (file: File) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const contents = e.target?.result as string
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
          disabled={disabled}
        >
          <Button
            disabled={disabled}
            icon={<InboxOutlined />}
          >
            Upload
          </Button>
        </Upload>
      </div>
    </>
  )
}

function isValidTemplate(template: string): string {
  if (!template?.length) return "" // Template is optional
  let atLeastOne = false // Template should not be empty
  const lines = template.split("\n")
  if (lines[0].charAt(0) !== "@") {
    return 'Error: The first character of the first line should be "@"'
  }
  let isConfigurationLine = false
  for (const line of lines) {
    if (line.length === 0) {
      // skip line if empty
      continue
    }
    if (line.charAt(0) === "@") {
      atLeastOne = true
      isConfigurationLine = true
      continue
    }
    if (isConfigurationLine) {
      if (line.charAt(0) === ">") {
        const isDescription = line.length >= 13 && line.substring(0, 13).toLowerCase() === ">description="
        // option lines
        if (line.toLowerCase() !== ">required" && line.toLowerCase() !== ">optional" && !isDescription) {
          return 'Error: Option lines should be either ">Required", ">Optional" or start with ">Description="'
        }
      } else {
        isConfigurationLine = false
      }
    }
  }
  if (!atLeastOne) {
    return "Error: Template should not be empty"
  }
  return ""
}

const DockerFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const {message} = useAppApi()
  const dockerImage = Form.useWatch("dockerImage", form)

  const dockerDisabled = !dockerImage?.length


  const normFile = (e: any) => {
    console.log('Upload event:', e);
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };

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

      <>
        <Form.Item
          rules={[{ required: !dockerDisabled, message: "Docker script is required" }]}
          label="Docker start script"
          name="dockerScript"
          tooltip="TODO write docs for this"
        >
          <Input.TextArea
            disabled={dockerDisabled}
            autoSize={{ minRows: 3 }}
            style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto" }}
          />
        </Form.Item>
        {/* <UploadBtn
          form={form}
          disabled={dockerDisabled}
          fieldName="dockerScript"
        /> */}

        <Form.Item
          label="Docker template"
          name="dockerTemplate"
          tooltip="TODO write docs for this"
          rules={[
            {
              validator: (_, value) => {
                const errorMessage = isValidTemplate(value)
                return errorMessage === "" ? Promise.resolve() : Promise.reject(new Error(errorMessage))
              },
            },
          ]}
        >
          <Input.TextArea
            autoSize={{ minRows: 3 }}
            disabled={dockerDisabled}
            style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto" }}
          />
        </Form.Item>
        {/* <UploadBtn
          form={form}
          disabled={dockerDisabled}
          fieldName="dockerTemplate"
        /> */}
      </>

      <Form.Item
        label="Docker test directory"
        name="dockerTestDir"
        tooltip="TODO write docs for this"
        valuePropName="fileList"
        getValueFromEvent={normFile}
      >
        <Upload
          listType="picture"
          maxCount={1}
          disabled={dockerDisabled}
          accept="application/zip, application/x-zip-compressed, application/octet-stream, application/x-zip, *.zip"
          beforeUpload={ (file) => {
            const isZIP = file.type.includes('zip') || file.name.includes('.zip')
            if (!isZIP) {
              message.error(`${file.name} is not a zip file`);
              return Upload.LIST_IGNORE
            }
            return false
          }}
        >
          <Button disabled={dockerDisabled} icon={<UploadOutlined />}>Upload test directory (zip)</Button>
        </Upload>
      </Form.Item>
    </>
  )
}

export default DockerFormTab
