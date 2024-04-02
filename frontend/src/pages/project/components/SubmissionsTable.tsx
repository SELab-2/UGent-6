import { Button, Input, List, Table, Typography } from "antd"
import { FC, useMemo } from "react"
import { ProjectSubmissionsType } from "./SubmissionsCard"
import { TableProps } from "antd/lib"
import { useTranslation } from "react-i18next"
import { DownloadOutlined } from "@ant-design/icons"
import useProject from "../../../hooks/useProject"
import SubmissionStatusTag from "./SubmissionStatusTag"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"

const GroupMember = ({ name, surname }: ProjectSubmissionsType["group"]["members"][number]) => {
  return <List.Item>{name + " " + surname}</List.Item>
}

const SubmissionsTable: FC<{ submissions: ProjectSubmissionsType[] | null }> = ({ submissions }) => {
  const { t } = useTranslation()
  const project = useProject()

  const updateScore = (s: ProjectSubmissionsType, score: string) => {
    // TODO: update score
  }

  const updateFeedback = (s: ProjectSubmissionsType, feedback: string) => {
    // TODO: update feedback
  }

  const columns: TableProps<ProjectSubmissionsType>["columns"] = useMemo(() => {
    return [
      {
        title: t("project.group"),
        dataIndex: "group",
        render: (g) => <Typography.Text>{g.name}</Typography.Text>,
        description: "test",
      },
      {
        title: t("project.submission"),
        render: (s:ProjectSubmissionsType) => <Link to={AppRoutes.SUBMISSION.replace(":submissionID", s.submissionId+"")}><Button type="link">#{s.submissionId}</Button></Link>,
      },
      {
        title: t("project.status"),
        render: (s) => <Typography.Text><SubmissionStatusTag docker_accepted={s.docker_accepted} structure_accepted={s.structure_accepted}/> </Typography.Text>,
      },
      {
        title: t("project.submissionTime"),
        dataIndex: "submitted_time",
        render: (time) => <Typography.Text>{new Date(time).toLocaleString()}</Typography.Text>,
      },
      {
        title: `Score (${project?.maxScore ?? ""})`,
        render: (s: ProjectSubmissionsType) => <Typography.Text editable={{ onChange: (e) => updateScore(s, e), maxLength: 10 }}>{s.feedback.score ?? "-"}</Typography.Text>,
      },
      {
        title: "Download",
        render: () => (
          <Button
            type="text"
            icon={<DownloadOutlined />}
          />
        ),
        align: "center",
      },
    ]
  }, [t,project])

  return (
    <Table
      loading={submissions === null}
      dataSource={submissions ?? []}
      locale={{ emptyText: submissions === null ? t("project.loadingSubmissions") : t("project.noSubmissions") }}
      expandable={{
        expandedRowRender: (g) => (
          <>
          <div style={{marginBottom:"3rem"}}>

            <Typography.Text strong>{t("project.feedback")}:</Typography.Text>
            <br/><br/>
            <Typography.Paragraph
              editable={{
                autoSize: { maxRows: 5, minRows: 3 },
                onChange: (value)=> updateFeedback(g, value),
              }}
              >
              {g.feedback.feedback || "-"}
            </Typography.Paragraph>
              </div>
           
            <Typography.Text strong>Group members:</Typography.Text>
            <List
              locale={{ emptyText: t("project.groupEmpty") }}
              dataSource={g.group.members ?? []}
              renderItem={GroupMember}
            />
          </>
        ),
      }}
      rowKey="submissionId"
      pagination={false}
      columns={columns}
    />
  )
}

export default SubmissionsTable
