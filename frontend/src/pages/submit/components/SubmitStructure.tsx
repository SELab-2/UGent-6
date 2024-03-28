import { Tree } from "antd"
import type { TreeDataNode } from "antd"
import { FC } from "react"

const treeData: TreeDataNode[] = [
  {
    title: "src",
    key: "0-0",
    children: [
      {
        title: "server",
        key: "0-0-0",
        children: [
          {
            title: "index.js",
            key: "0-0-0-0",
            isLeaf: true,
          },
          {
            title: "*.js",
            key: "0-0-0-1",
            isLeaf: true,
          },
        ],
      },
      {
        title: "client",
        key: "0-0-1",
        children: [
          {
            title: "Index.jsx",
            key: "0-0-1-0",
            isLeaf: true,
          },
          {
            title: "*.jsx",
            key: "0-0-1-1",
            isLeaf: true,
          },
        ],
      },
    ],
  },
  {
    title: "verslag",
    key: "0-1",
    children: [
      { title: "verslag.pdf", key: "0-1-0", isLeaf: true },
      { title: "testen.pdf", key: "0-1-1", isLeaf: true },
    ],
  },
  {
    title: "package.json",
    key: "0-2",
    isLeaf: true,
  },
  {
    title: "node_modules",
    key: "0-3",
    style: {
      color: "red",
    },
  },
]

const SubmitStructure: FC<{ structure: string }> = ({ structure }) => {
  return (
    <Tree.DirectoryTree
      multiple
      defaultExpandAll
      selectable={false}
      treeData={treeData}
    />
  )
}

export default SubmitStructure
