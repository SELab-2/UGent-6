import { InboxOutlined, UploadOutlined } from "@ant-design/icons"
import {Button, Dropdown, Form, Input, Menu, Select, Switch, Upload} from "antd"
import { TextAreaProps } from "antd/es/input"
import { FormInstance } from "antd/lib"
import React, {FC, useEffect, useLayoutEffect, useState} from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes } from "../../../@types/requests"
import useAppApi from "../../../hooks/useAppApi"
import MarkdownTooltip from "../../common/MarkdownTooltip"
import { classicNameResolver } from "typescript"
import MarkdownTextfield from "../../input/MarkdownTextfield"
import TextArea from "antd/es/input/TextArea";

import BashIcon from "../../../../public/docker_langauges/bash.svg"
import PythonIcon from "../../../../public/docker_langauges/python.svg"
import NodeIcon from "../../../../public/docker_langauges/node-js.svg"
import HaskellIcon from "../../../../public/docker_langauges/haskell.svg"
import Custom from "../../../../public/docker_langauges/custom.svg"

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
  image: string;
  icon: string;
}

interface ScriptCollection {
  [key: string]: Script;
}

export const languageOptions: ScriptCollection = {
  "bash": { displayName: "Bash", image: "fedora", icon: BashIcon },
  "python": { displayName: "Python", image: "python", icon: PythonIcon },
  "javascript": { displayName: "Javascript (node)", image: "node", icon: NodeIcon },
  "haskell": { displayName: "Haskell", image: "haskell", icon: HaskellIcon },
  "custom": { displayName: "Custom", image: "", icon: Custom }
}

export function imageToLanguage(script: string): string {
  for(const language in languageOptions) {
    if(script === languageOptions[language].image) {
      return language
    }
  }
  return ""
}


const DockerFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const {message} = useAppApi()
  const [withTemplate, setWithTemplate] = useState<boolean>(true)
  const [selectedLanguage, setSelectedLanguage] = useState<string>("bash")
  const dockerImage = Form.useWatch("dockerImage", form)

  const dockerDisabled = !dockerImage?.length

    useEffect(() =>  {

        form.validateFields(["dockerScript", "dockerTemplate"])
    }, [dockerDisabled])

  function isValidTemplate(template: string): string {
    if (template.length === 0) {
      return t("project.tests.dockerTemplateValidation.emptyTemplate")
    }
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
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };

  let switchClassName = 'template-switch'
  let scriptPlaceholder

  if (withTemplate) {
    switchClassName += ' template-switch-active'
    scriptPlaceholder = "bash /shared/input/helloworld.sh > \"/shared/output/helloWorldTest\"\n"+
    "bash /shared/input/helloug.sh > \"/shared/output/helloUGent\"\n"
  } else {
    switchClassName += ' template-switch-inactive'
    scriptPlaceholder = "output=$(bash /shared/input/helloworld.sh)\n"+
    "if [[ \"$output\" == \"Hello World\" ]]; then \n"+
    "  echo 'Test one is successful\n"+
    "  echo 'PUSH ALLOWED' > /shared/output/testOutput\n"+
    "else\n"+
    "  echo 'Test one failed: script failed to print \"Hello World\"'\n"+
    "fi"
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
          name="dockerImage"
      >
        <Input.Group compact>
          <Form.Item
              name="languageSelect"
              noStyle
          >
            <Select
                defaultValue={Object.keys(languageOptions)[0]}
                onChange={(val) => form.setFieldValue( "dockerImage", languageOptions[val].image)}
                style={{ width: '30%' }}
            >
              {Object.keys(languageOptions).map((key) => (
                  <Select.Option key={key} value={key}>
            <span style={{ marginRight: 8 }}>
              <img src={languageOptions[key].icon} alt={languageOptions[key].displayName} style={{ height: '16px', verticalAlign: 'middle' }} />
            </span>
                    {languageOptions[key].displayName}
                  </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
              name="dockerImage"
              noStyle
          >
            <Input
                style={{ width: '70%' }}
                placeholder={t("project.tests.dockerImagePlaceholder")}
            />
          </Form.Item>
        </Input.Group>
      </Form.Item>
      <>
        <Form.Item
          rules={[{ required: !dockerDisabled, message: t("project.tests.dockerScriptRequired") }]}
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
            autoSize={{ minRows: 8 }}
            style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto"}}
            placeholder={scriptPlaceholder}
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
                      value ??= ""
                      if (dockerDisabled) {
                        return Promise.resolve()
                      }
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
