import { Input, Tabs, TabsProps } from "antd"
import { TextAreaProps } from "antd/lib/input"
import { FC, useMemo, useState } from "react"
import MarkdownTextfield from "./MarkdownTextfield"
import { useTranslation } from "react-i18next"

const MarkdownEditor: FC<{ value?: string | null } & TextAreaProps> = ({ ...args }) => {
  const {t} = useTranslation()
  const [value, setValue] = useState<string>("")


  const items: TabsProps["items"] = useMemo(()=>([
    {
      key: "write",
      label: t("components.write"),
      children: <div style={{paddingTop:"3px"}}><Input.TextArea onChange={(e) => setValue(e.target.value)}  rows={4}  {...args} /></div>,
    },
    {
      key: "preview",
      label: t("components.preview"),
      children: <MarkdownTextfield content={args.value ?? value} />,
    },
  ]), [value])

  return <Tabs tabBarStyle={{padding:0, margin:0}} type="card" items={items} />
}

export default MarkdownEditor
