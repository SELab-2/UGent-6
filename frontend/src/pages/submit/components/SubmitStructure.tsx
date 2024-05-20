import { Tree, Typography } from "antd"
import type { TreeDataNode } from "antd"
import { FC, memo, useMemo } from "react"
import { useTranslation } from "react-i18next"

type TreeDataOutput = { tree: TreeDataNode[] | null; error: string | null, directoryIds: string[] }

type TreeNode = {
  title: string
  key: string
  isLeaf: boolean
  style?: { color: string }
  children?: TreeNode[]
}

function getPrefixLength(line: string): number {
  let prefixLength = 0
  while (prefixLength < line.length && (line[prefixLength] === " " || line[prefixLength] === "\t")) {
    prefixLength++
  }
  return prefixLength
}

function parseSubmissionTemplate(lines: string[], directoryIds: string[], prefix = "", key = ""): TreeNode[] {
  let children: TreeNode[] = []

  while (lines.length > 0) {
    const leadingWhitespaces = getPrefixLength(lines[0])
    if (leadingWhitespaces < prefix.length) break

    const line = lines.shift()?.trimEnd()
    let newKey = key + line
    if (!line?.length) continue // ignore empty lines
    let node: TreeNode = {
      title: line.trim(),
      isLeaf: true,
      key: newKey,
      style: undefined,
      children: [],
    }

    if (line.trimStart().startsWith("-")) {
      // ignore file
      node.style = { color: "#F44336" }
      node.title = node.title.substring(1)
    }

    if (line.endsWith("/")) {
      node.title = node.title.substring(0, node.title.length - 1)
      // It's a directory
      node.isLeaf = false
      directoryIds.push(newKey)
      if (lines[0]) {
        const nextLineWhitespaces = getPrefixLength(lines[0])
        if (nextLineWhitespaces > leadingWhitespaces) {
          node.children = parseSubmissionTemplate(lines, directoryIds, lines[0].substring(0, nextLineWhitespaces), newKey)
        }
      }
    }
    children.push(node)
  }

  return children
}

export function generateTreeData(structure: string): TreeDataOutput {
  if (!structure) return { tree: null, error: "No structure", directoryIds:[] }
  // Remove comments (lines that include # until end of the line)
  structure = structure.replace(/#.*?(?=\n)/g, "")
  // Split the string into lines
  const lines = structure.split("\n")
  let result: TreeNode[] = []
  let directoryIds: string[] = []
  try {
    result = parseSubmissionTemplate(lines, directoryIds)
  } catch (error) {
    console.error(error)
    return { tree: null, error: "Woops something went wrong while parsing!",directoryIds:[] } // If you get this, then there's a bug in the parser
  }

  return {
    tree: result,
    error: null,
    directoryIds
  }
}

const SubmitStructure: FC<{ structure: string | null; hideEmpty?: boolean }> = ({ structure, hideEmpty }) => {
  const { t } = useTranslation()
  const treeData: TreeDataOutput = structure === null ? { tree: [{ isLeaf: true, title: "Loading...", key: "loading" }], error: null,directoryIds:[] } : generateTreeData(structure)

  if (structure === "" && !hideEmpty) return <Typography.Text type="secondary">{t("project.noStructure")}</Typography.Text>
  if (!treeData.tree) return null
  return (
    <Tree.DirectoryTree
      multiple
      defaultExpandAll
      autoExpandParent
      expandedKeys={treeData.directoryIds}
      loadedKeys={!structure ? undefined : ["loading"]}
      selectable={false}
      treeData={treeData.tree}
    />
  )
}

export default memo(SubmitStructure)
