import { Input } from "antd"
import { TextAreaProps } from "antd/lib/input"
import {  FC } from "react"

const MarkdownEditor: FC<{  } & TextAreaProps> = ({ ...args }) => {
  // TODO: Implement markdown editor
  return (
    <>
      <Input.TextArea
        {...args}
      />
    </>
  )
}

export default MarkdownEditor
