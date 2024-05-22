import Markdown from "react-markdown"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { oneDark, oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"
import useApp from "../../hooks/useApp"
import { FC } from "react"

const MarkdownTextfield: FC<{ content: string, inTooltip?: boolean}> = ({ content, inTooltip }) => {
  const app = useApp()

  const CodeBlock = {
    code({ children, className, node, ...rest }: any) {
      const match = /language-(\w+)/.exec(className || "")
      return match ? (
        <SyntaxHighlighter
          {...rest}
          PreTag="div"
          children={String(children).replace(/\n$/, "")}
          language={match[1]}
          style={app.theme === "light" ? oneLight : oneDark}
        />
      ) : (
        <code
          {...rest}
          className={className}
        >
          {children}
        </code>
      )
    },
  }

  let className = 'markdown-textfield'
  console.log(inTooltip)
  if (inTooltip) {
    className = 'markdown-textfield-intooltip'
  }

  return <Markdown components={CodeBlock} className={className}>{content}</Markdown>
}

export default MarkdownTextfield
