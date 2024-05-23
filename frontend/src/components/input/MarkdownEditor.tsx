import { Form, Input, Tabs, TabsProps } from "antd"
import { TextAreaProps } from "antd/lib/input"
import { FC, useMemo, useState } from "react"
import MarkdownTextfield from "./MarkdownTextfield"
import { useTranslation } from "react-i18next"

const MarkdownEditor: FC<{ value?: string | null; onChange?: (value: string) => void } & TextAreaProps> = ({ onChange, value, ...args }) => {
  const { t } = useTranslation()

  const items: TabsProps["items"] = useMemo(
    () => [
      {
        key: "write",
        label: t("components.write"),
        children: (
          <div style={{ paddingTop: "3px" }}>
            <Form.Item
              rules={[{ max: 5000, message: t("home.courseDescriptionMaxLength") }]}
              name="description"
            >
              <Input.TextArea
                autoSize={{ minRows: 4, maxRows: 20 }}

                
                {...args}
              />
            </Form.Item>
          </div>
        ),
      },
      {
        key: "preview",
        label: t("components.preview"),
        children: <MarkdownTextfield content={value as string} />,
        disable: !value,
      },
    ],
    [value]
  )

  return (
    <Tabs
      tabBarStyle={{ padding: 0, margin: 0 }}
      type="card"
      items={items}
    />
  )
}

export default MarkdownEditor
