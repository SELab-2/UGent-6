import { MDXProvider } from "@mdx-js/react"
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter"
import { oneDark, oneLight } from "react-syntax-highlighter/dist/esm/styles/prism"
import useApp from "../../hooks/useApp"
import { FC } from "react"

const MarkdownTextfield: FC<{ content: string }> = ({ content }) => {
  const app = useApp()

  const components = {
    code({ children, className, ...rest }: any) {
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
        <code {...rest} className={className}>
          {children}
        </code>
      )
    },
  }

  return (
    <MDXProvider components={components}>
      <div>{content}</div>
    </MDXProvider>
  )
}

export default MarkdownTextfield
