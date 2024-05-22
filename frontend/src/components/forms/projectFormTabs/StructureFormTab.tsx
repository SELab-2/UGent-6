import { Form, Input, Typography, Tooltip, Space } from "antd"
import { QuestionCircleOutlined } from "@ant-design/icons"
import { FC } from "react"
import SubmitStructure from "../../../pages/submit/components/SubmitStructure"
import { useTranslation } from "react-i18next"
import { FormInstance } from "antd/lib"
import { useDebounceValue } from "usehooks-ts"
import MarkdownTooltip from "../../common/MarkdownTooltip"

const StructureFormTab: FC<{ form: FormInstance }> = ({ form }) => {
  const { t } = useTranslation()
  const structure = Form.useWatch("structureTest", form)
  const [debouncedValue] = useDebounceValue(structure, 400)


  return (
    <>
      <Form.Item
        label={
            <MarkdownTooltip
              label={t("project.tests.fileStructure")}
              tooltipContent={t("project.tests.fileStructureTooltip")}
              placement="right"
            />
        }
        name="structureTest"
      >
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
      <SubmitStructure structure={debouncedValue} hideEmpty />
    </>
  )
}

export default StructureFormTab
