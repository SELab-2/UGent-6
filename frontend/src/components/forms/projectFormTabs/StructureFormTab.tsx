import { Form, Input, Typography } from "antd"
import { FC } from "react"
import SubmitStructure from "../../../pages/submit/components/SubmitStructure"
import { useTranslation } from "react-i18next"
import { FormInstance } from "antd/lib"
import { useDebounceValue } from "usehooks-ts"

const StructureFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const structure = Form.useWatch("structure", form)
  const [debouncedValue] = useDebounceValue(structure, 400)

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
          onKeyDown={(e) => {
            if (e.key === "Tab") {
              e.preventDefault()
              const start = e.currentTarget.selectionStart
              const end = e.currentTarget.selectionEnd
              e.currentTarget.value = e.currentTarget.value.substring(0, start) + "\t" + e.currentTarget.value.substring(end)
              e.currentTarget.selectionStart = e.currentTarget.selectionEnd = start + 1
            }
          }}
        />
      </Form.Item>

      <Typography.Text strong>{t("project.change.fileStructurePreview")}:</Typography.Text>
      <SubmitStructure structure={debouncedValue} />
    </>
  )
}

export default StructureFormTab
