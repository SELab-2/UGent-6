import { InboxOutlined, UploadOutlined } from "@ant-design/icons"
import {Button, Dropdown, Form, Input, Menu, Select, Switch, Upload} from "antd"
import { TextAreaProps } from "antd/es/input"
import { FormInstance } from "antd/lib"
import {FC, useEffect, useState} from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes } from "../../../@types/requests"
import useAppApi from "../../../hooks/useAppApi"
import MarkdownTooltip from "../../common/MarkdownTooltip"
import { classicNameResolver } from "typescript"
import MarkdownTextfield from "../../input/MarkdownTextfield"
import TextArea from "antd/es/input/TextArea";

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

interface Script {
  displayName: string;
  scriptGenerator: (script: string) => string;
  image?: string;
}

interface ScriptCollection {
  [key: string]: Script;
}

const DockerFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const {message} = useAppApi()
  const [withTemplate, setWithTemplate] = useState<boolean>(true)
  const [imageSelect, setImageSelect] = useState<string>("alpine")
  const [dockerDisabled, setDockerImage] = useState<boolean>(false)



  const languageOptions:ScriptCollection = {
    "bash": {displayName:"Bash", scriptGenerator: (script: string) => script, image: "fedora"},
    "python": {displayName:"Python", scriptGenerator: (script: string) => `python -c '${script}'`, image: "python"},
    "javascript": {displayName:"Javascript (node)", scriptGenerator: (script: string) => `node -e '${script}', image: "node"`},
    "haskell": {displayName:"Haskell", scriptGenerator: (script: string) => `runhaskell -e '${script}'`, image: "haskell"}
  }

  useEffect(() => {
    setDockerImage(imageSelect.length == 0)
  }, [imageSelect]);

  function isValidTemplate(template: string): string {
    if (!template?.length) return "" // Template is optional
    let atLeastOne = false // Template should not be empty
    const lines = template.split("\n")
    if (lines[0].charAt(0) !== "@") {
      return t("project.tests.dockerTemplateValidation.inValidFirstLine")
    }
    let isConfigurationLine = false
    let lineNumber = 0
    for (const line of lines) {
      lineNumber++
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
            return t("project.tests.dockerTemplateValidation.inValidOptions", { line:lineNumber.toString() })
          }
        } else {
          isConfigurationLine = false
        }
      }
    }
    if (!atLeastOne) {
      return t("project.tests.dockerTemplateValidation.emptyTemplate")
    }
    return ""
  }


  const normFile = (e: any) => {
    console.log('Upload event:', e);
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };

  let switchClassName = 'template-switch'
  if (withTemplate) {
    switchClassName += ' template-switch-active'
  } else {
    switchClassName += ' template-switch-inactive'
  }

  return (
    <>
      <Form.Item
        label={
          <MarkdownTooltip
            label={"Docker Image"}
            tooltipContent={t("project.tests.dockerImageTooltip")}
            placement="right"
          />
        }
        //name = "dockerImage"    //Als je deze uncomment dan werkt de value van de input niet meer.
      >
        <Input
          style={{ marginTop: "8px" }}
          value={imageSelect}
          onChange={(event) => setImageSelect(event.target.value)}
        />
      </Form.Item>
      <Form.Item
      >
        <Input
          value = {"WAHOOO"}
        />

      </Form.Item>

      <Select defaultValue={Object.keys(languageOptions)[0]} onChange={(val) => setImageSelect(val)} >
        {Object.keys(languageOptions).map((key) => (
            <Select.Option value={key}>{languageOptions[key].displayName}</Select.Option>
        ))}
      </Select>

      <>
        <Form.Item
          rules={[{ required: !dockerDisabled, message: "Docker script is required" }]}
          label={
            <MarkdownTooltip
              label={"Docker start script"}
              tooltipContent={t("project.tests.dockerScriptTooltip")}
              placement="right"
            />
          }
          name="dockerScript"
        >
          <TextArea
            disabled={dockerDisabled}
            autoSize={{ minRows: 3 }}
            style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto" }}
          />
        </Form.Item>
        <Form.Item
        label={
          <MarkdownTooltip
            label={"Docker test directory"}
            tooltipContent={t("project.tests.dockerTestDirTooltip")}
            placement="right"
          />
        }
        name="dockerTestDir"
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
        {/* <UploadBtn
          form={form}
          disabled={dockerDisabled}
          fieldName="dockerScript"
        /> */}
        <div style={{ paddingBottom: '14px'}}>
          <Switch
              checked={withTemplate}
              checkedChildren={t("project.tests.templateMode")}
              unCheckedChildren={t("project.tests.simpleMode")}
              onChange={setWithTemplate}
              className={switchClassName}
          />
        </div>

        {withTemplate ?
            <div>
              <MarkdownTextfield content={t("project.tests.templateModeInfo")} />

            <Form.Item
                label={t("project.tests.dockerTemplate")}
                name="dockerTemplate"
                rules={[
                  {
                    validator: (_, value) => {
                      const errorMessage = isValidTemplate(value)
                      return errorMessage === "" ? Promise.resolve() : Promise.reject(new Error(errorMessage))
                    }, required: true
                  }
                ]}
            >

              <Input.TextArea
                  autoSize={{minRows: 4}}
                  disabled={dockerDisabled}
                  style={{fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto"}}
                  placeholder={"@helloWorldTest\n>required\n>description=\"This is a test\"\nExpected output 1\n\n@helloUGent\n>optional\nExpected output 2\n"}
              />
                {/*<UploadBtn
                  form={form}
                  disabled={dockerDisabled}
                  fieldName="dockerTemplate"
              />*/}
            </Form.Item> </div>: <Form.Item
          name="simpleMode"
          children={<MarkdownTextfield content={t("project.tests.simpleModeInfo")} />}
            rules={[{ required: false}]}
        />}
      </>
    </>
  )
}

export default DockerFormTab
