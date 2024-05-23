import { CodepenCircleFilled, InboxOutlined, UploadOutlined } from "@ant-design/icons"
import { Button, Dropdown, Form, Input, Menu, Select, SelectProps, Switch, Upload } from "antd"
import { TextAreaProps } from "antd/es/input"
import { FormInstance } from "antd/lib"
import React, { FC, useEffect, useLayoutEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import useAppApi from "../../../hooks/useAppApi"
import MarkdownTooltip from "../../common/MarkdownTooltip"
import MarkdownTextfield from "../../input/MarkdownTextfield"
import TextArea from "antd/es/input/TextArea"

import BashIcon from "../../../../public/docker_langauges/bash.svg"
import PythonIcon from "../../../../public/docker_langauges/python.svg"
import NodeIcon from "../../../../public/docker_langauges/node-js.svg"
import HaskellIcon from "../../../../public/docker_langauges/haskell.svg"
import Custom from "../../../../public/docker_langauges/custom.svg"


type DockerLanguage = "bash" | "python" | "node" | "haskell" | "custom"
const languageOptions: Record<DockerLanguage, string> = {
  bash: "fedora",
  python: "python",
  node: "node",
  haskell: "haskell",
  custom: ""
}

const imageToLanguage: Record<string, DockerLanguage> = {
  fedora: "bash",
  python: "python",
  node: "node",
  haskell: "haskell",
}


const languagesSelectorItems:SelectProps["options"] = [
  {
    label:  <><img src={BashIcon} className="select-icon" />Bash</>,
    value: "bash",
  },{
    label:  <><img src={PythonIcon} className="select-icon" />Python</>,
    value: "python",
  }, {
    label:  <><img src={NodeIcon} className="select-icon" />NodeJS</>,
    value: "node",
  }, {
    label:  <><img src={HaskellIcon} className="select-icon" />Haskell</>,
    value: "haskell",
  }, {
    label:  <><img src={Custom} className="select-icon" />Custom</>,
    value: "custom",
  }
]



const DockerFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const { message } = useAppApi()
  const dockerImage = Form.useWatch("dockerImage", form)
  const dockerTemplate = Form.useWatch("dockerTemplate", form)
  const dockerMode = Form.useWatch("dockerMode", form)

  const dockerDisabled = !dockerImage?.length


  const withTemplate = (dockerMode === null && !!dockerTemplate?.length) || !!dockerMode

 
    useEffect(() =>  {
      
        form.validateFields(["dockerScript", "dockerTemplate"])
    }, [dockerDisabled])

    
  const dockerImageSelect= useMemo(()=>  imageToLanguage[dockerImage] || "custom",[dockerImage])

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
            return t("project.tests.dockerTemplateValidation.inValidOptions", { line: lineNumber.toString() })
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
      return e
    }
    return e?.fileList
  }

  let switchClassName = "template-switch"
  let scriptPlaceholder

  if (withTemplate) {
    switchClassName += " template-switch-active"
    scriptPlaceholder = 'bash /shared/input/helloworld.sh > "/shared/output/helloWorldTest"\n' + 'bash /shared/input/helloug.sh > "/shared/output/helloUGent"\n'
  } else {
    switchClassName += " template-switch-inactive"
    scriptPlaceholder = "output=$(bash /shared/input/helloworld.sh)\n" + 'if [[ "$output" == "Hello World" ]]; then \n' + "  echo 'Test one is successful\n" + "  echo 'PUSH ALLOWED' > /shared/output/testOutput\n" + "else\n" + "  echo 'Test one failed: script failed to print \"Hello World\"'\n" + "fi"
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
        <Input
          addonBefore={
            <Select
              style={{ width: 150 }}
              value={dockerImageSelect}
              onChange={(val:DockerLanguage) => form.setFieldValue("dockerImage", languageOptions[val])}
              options={languagesSelectorItems}
            />}
          placeholder={t("project.tests.dockerImagePlaceholder")}
        />
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
            style={{ fontFamily: "monospace", whiteSpace: "pre", overflowX: "auto" }}
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
        {/* <UploadBtn
          form={form}
          disabled={dockerDisabled}
          fieldName="dockerScript"
        /> */}
        <div style={{ paddingBottom: '14px'}}>
          <Form.Item name="dockerMode" label="" valuePropName="checked">

          <Switch
              checkedChildren={t("project.tests.templateMode")}
              unCheckedChildren={t("project.tests.simpleMode")}
              className={switchClassName}
              />
              </Form.Item>
        </div>

        
          <div style={withTemplate ? {} : {display: "none"}}>
            <MarkdownTextfield content={t("project.tests.templateModeInfo")} />

            <Form.Item
                label={t("project.tests.dockerTemplate")}
                name="dockerTemplate"
                rules={[
                  {
                    validator: (_, value) => {
                      value ??= ""
                      if (dockerDisabled || !withTemplate) {
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
                {/*<UploadBtn
                  form={form}
                  disabled={dockerDisabled}
                  fieldName="dockerTemplate"
              />*/}
            </Form.Item> 
          </div> 

          <Form.Item
            name="simpleMode"
            rules={[{ required: false}]}
            style={withTemplate ? {display: "none"} : {}}
          ><MarkdownTextfield content={t("project.tests.simpleModeInfo")} /></Form.Item>
      </>
    </>
  )
}

export default DockerFormTab