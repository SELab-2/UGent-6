import { Form, Input, Typography } from "antd"
import { FC, useState } from "react"
import SubmitStructure from "../../../pages/submit/components/SubmitStructure"
import { useTranslation } from "react-i18next"
import { FormInstance } from "antd/lib"

const StructureFormTab: FC<{form:FormInstance}> = ({form}) => {
  const {t} = useTranslation()
  const structure = Form.useWatch("structure", form)

  return (
    <>
      <Form.Item
        label={t("project.change.fileStructure")}
        name="structure"
        tooltip="TODO write docs for this"

      >
        <Input.TextArea
          autoSize={{ minRows: 3 }}
          style={{ fontFamily: "monospace" }}
        />
      </Form.Item>

    <Typography.Text strong >
      {t("project.change.fileStructurePreview")}:
    </Typography.Text>
      <SubmitStructure structure={structure} />
    </>
  )
}

export default StructureFormTab
