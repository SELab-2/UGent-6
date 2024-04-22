import { Input, Tabs, TabsProps } from "antd"
import { TextAreaProps } from "antd/lib/input"
import { FC } from "react"
import MarkdownTextfield from "./MarkdownTextfield"
import { useTranslation } from "react-i18next"

const MarkdownEditor: FC<{ value: string | null } & TextAreaProps> = ({ ...args }) => {
  // TODO: Implement markdown editor
  const {t} = useTranslation()


  const items: TabsProps["items"] = [
    {
      key: "write",
      label: t("components.write"),
      children: <Input.TextArea  rows={4}  {...args} />,
    },
    {
      key: "preview",
      label: t("components.preview"),
      children: <MarkdownTextfield content={args.value} />,
    },
  ]

  return <Tabs tabBarStyle={{padding:0, margin:0}} type="card" items={items} />
}

export default MarkdownEditor
