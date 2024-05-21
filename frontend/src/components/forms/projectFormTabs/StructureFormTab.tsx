import { Form, Input, Typography } from "antd"
import { FC } from "react"
import SubmitStructure from "../../../pages/submit/components/SubmitStructure"
import { useTranslation } from "react-i18next"
import { FormInstance } from "antd/lib"
import { useDebounceValue } from "usehooks-ts"

const StructureFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const structure = Form.useWatch("structureTest", form)
  const [debouncedValue] = useDebounceValue(structure, 400)

  return (
    <>
      <Form.Item
        label={t("project.change.fileStructure")}
        name="structureTest"
        tooltip={t("project.tests.fileStructureTooltip")}>
        <Input.TextArea
          autoSize={{ minRows: 5 }}
          style={{ fontFamily: "monospace" }}
          placeholder={
              'src/\n' +
              '  index.js\n' +
              '  \\\.*\n'+
              'common/\n' +
              '  index.css\n' +
              '-node_modules/\n'}
          onKeyDown={(e) => {
            if (e.key === "Tab") {
              e.preventDefault()
              const start = e.currentTarget.selectionStart
              const end = e.currentTarget.selectionEnd
              e.currentTarget.value = e.currentTarget.value.substring(0, start) + "\t" + e.currentTarget.value.substring(end)
              e.currentTarget.selectionStart = e.currentTarget.selectionEnd = start + 1
              form.setFieldValue("structureTest", e.currentTarget.value)
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
