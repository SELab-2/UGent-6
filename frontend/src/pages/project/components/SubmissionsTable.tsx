import {  Button, Table, Typography } from "antd"
import { FC,  useMemo } from "react"
import { ProjectSubmissionsType } from "./SubmissionsCard"
import { TableProps } from "antd/lib"
import { useTranslation } from "react-i18next"
import { DownloadOutlined } from "@ant-design/icons"

const SubmissionsTable: FC<{ submissions: ProjectSubmissionsType[] | null }> = ({ submissions }) => {
  const { t } = useTranslation()

  const updateScore = (s: ProjectSubmissionsType, score: string) => {}

  const columns: TableProps<ProjectSubmissionsType>["columns"] = useMemo(() => {
    return [
      {
        title: "Group",
        dataIndex: "group",
        render: (g) => <Typography.Text>{g.name}</Typography.Text>,
        description: "test"
      },
      {
        title: "Status",
        render: (s) => <Typography.Text>{s.docker_accepted ? "Accepted" : "Failed"}</Typography.Text>,
      },
      {
        title: "Score",
        render: (s: ProjectSubmissionsType) => <Typography.Text editable={{ onChange: (e) => updateScore(s, e), maxLength: 10 }}>{s.feedback.score ?? "-"}</Typography.Text>,
      },
      {
        title:"",
        render: () =>         <Button type="text" icon={<DownloadOutlined/>}/>,
        align: "center"
      }
    ]
  }, [t])

  return (
    <Table
      loading={!submissions}
      dataSource={submissions ?? []}
      expandable={{
        expandedRowRender: (record) => <p style={{ margin: 0 }}>{"dsfjk"}</p>,
      }}

      rowKey="submissionId"
      pagination={false}
      columns={columns}
    />
  )
}

export default SubmissionsTable
