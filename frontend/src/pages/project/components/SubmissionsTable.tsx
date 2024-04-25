import { Button, Input, List, Table, Typography } from "antd"
import { FC, useMemo } from "react"
import { ProjectSubmissionsType } from "./SubmissionsTab"
import { TableProps } from "antd/lib"
import { useTranslation } from "react-i18next"
import { DownloadOutlined } from "@ant-design/icons"
import useProject from "../../../hooks/useProject"
import SubmissionStatusTag, { createStatusBitVector } from "./SubmissionStatusTag"
import { Link, useParams } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"

const GroupMember = ({ name }: ProjectSubmissionsType["group"]["members"][number]) => {
  return <List.Item>{name }</List.Item>
}

const SubmissionsTable: FC<{ submissions: ProjectSubmissionsType[] | null }> = ({ submissions }) => {
  const { t } = useTranslation()
  const project = useProject()
  const {courseId} = useParams()

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
        key:"group",
        render: (g) => <Typography.Text>{g.name}</Typography.Text>,
        description: "test",
      },
      {
        title: t("project.submission"),
        key:"submissionId",
        render: (s:ProjectSubmissionsType) => <Link to={AppRoutes.SUBMISSION.replace(":submissionId", s.submission?.submissionId+"").replace(":projectId", s.submission?.projectId+"").replace(":courseId", courseId!)}><Button type="link">#{s.submission?.submissionId}</Button></Link>,
      },
      {
        title: t("project.status"),
        dataIndex: "submission",
        key:"submissionStatus",
        render: (s) => <Typography.Text><SubmissionStatusTag status={createStatusBitVector(s)}/> </Typography.Text>
      },
      {
        title: t("project.submissionTime"),
        dataIndex: "submission",
        key:"submission",
        render: (time:ProjectSubmissionsType["submission"]) => time?.submissionTime && <Typography.Text>{new Date(time.submissionTime).toLocaleString()}</Typography.Text>,
      },
      {
        title: `Score (${project?.maxScore ?? ""})`,
        key:"score",
        render: (s: ProjectSubmissionsType) => <Typography.Text editable={{ onChange: (e) => updateScore(s, e), maxLength: 10 }}>{s.feedback?.score ?? "-"}</Typography.Text>,
      },
      {
        title: "Download",
        key:"download",
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
              {g.feedback?.feedback || "-"}
            </Typography.Paragraph>
              </div>
           
            <Typography.Text strong>{t("project.groupMembers")}</Typography.Text>
            <List
              locale={{ emptyText: t("project.groupEmpty") }}
              dataSource={g.group.members ?? []}
              renderItem={GroupMember}
            />
          </>
        ),
      }}
      pagination={false}
      columns={columns}
    />
  )
}

export default SubmissionsTable
