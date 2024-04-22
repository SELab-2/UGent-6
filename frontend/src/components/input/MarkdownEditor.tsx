import { Input, Tabs, TabsProps } from "antd"
import { TextAreaProps } from "antd/lib/input"
import { FC } from "react"
import MarkdownTextfield from "./MarkdownTextfield"

const MarkdownEditor: FC<{ value: string | null } & TextAreaProps> = ({ ...args }) => {
  // TODO: Implement markdown editor

  const items: TabsProps["items"] = [
    {
      key: "write",
      label: "Write",
      children: <Input.TextArea  rows={4}  {...args} />,
    },
    {
      key: "preview",
      label: "Preview",
      children: <MarkdownTextfield content={args.value} />,
    },
  ]

  return <Tabs tabBarStyle={{padding:0, margin:0}} type="card" items={items} />
}

export default MarkdownEditor
