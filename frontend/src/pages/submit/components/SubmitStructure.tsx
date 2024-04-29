import { Tree } from "antd"
import type { TreeDataNode } from "antd"
import { FC, memo, useMemo } from "react"


type TreeDataOutput = { tree: TreeDataNode[] | null; error: string | null }


type TreeNode = {
  title: string;
  key: string;
  isLeaf: boolean;
  style?: { color: string };
  children?: TreeNode[];
};

function getPrefixLength(line: string): number {
  let prefixLength = 0;
  while (prefixLength < line.length && (line[prefixLength] === ' ' || line[prefixLength] === '\t')) {
    prefixLength++;
  }
  return prefixLength;
}

function parseSubmissionTemplate(lines: string[], prefix="",key=0): TreeNode[] {
    let children: TreeNode[] = [];

    while(lines.length > 0 ) {
      key++
      const leadingWhitespaces = getPrefixLength(lines[0])
      if( leadingWhitespaces < prefix.length) break

        const line = lines.shift()?.trimEnd();
        if (!line?.length) continue; // ignore empty lines
        let node:TreeNode  = {
          title: line.trim(),
          isLeaf: true,
          key: key.toString(),
          style:  undefined,
          children: [] 
        }

        if(line.trimStart().startsWith("-")) {
          // ignore file
          node.style = { color: "#F44336" }
          node.title = node.title.substring(1)
        }


        if (line.endsWith('/'))  {
          node.title = node.title.substring(0,node.title.length-1)
          // It's a directory
          node.isLeaf = false;
          if(lines[0]) {
            const nextLineWhitespaces = getPrefixLength(lines[0])
            if(nextLineWhitespaces > leadingWhitespaces) {
              node.children = parseSubmissionTemplate(lines,lines[0].substring(0,nextLineWhitespaces),key );
            }
          }
        }
        children.push(node);
    }

  return children;
}


export function generateTreeData(structure: string): TreeDataOutput {
  if (!structure) return { tree: null, error: "No structure" }
  // Remove comments (lines that include # until end of the line)
  structure = structure.replace(/#.*?(?=\n)/g, "")
  // Split the string into lines
  const lines = structure.split("\n")
  let result: TreeNode[] = []
  try {
     result = parseSubmissionTemplate(lines)

  } catch (error) {
    console.error(error)
    return { tree: null, error: "Woops something went wrong while parsing!" } // If you get this, then there's a bug in the parser
  }

  return {
    tree: result,
    error: null,
  }
}

const SubmitStructure: FC<{ structure: string }> = ({ structure }) => {
  const treeData: { tree: TreeDataNode[] | null; error: string | null } = generateTreeData(structure)

  
  if (!treeData.tree) return null
  return (
    <Tree.DirectoryTree
      multiple
      defaultExpandAll
      selectable={false}
      treeData={treeData.tree}
    />
  )
}

export default memo(SubmitStructure)
