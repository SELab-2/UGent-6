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
import apiCall from "../../../util/apiFetch"
import { ApiRoutes, PUT_Requests } from "../../../@types/requests.d"
import useAppApi from "../../../hooks/useAppApi"

const GroupMember = ({ name }: ProjectSubmissionsType["group"]["members"][number]) => {
  return <List.Item>{name}</List.Item>
}

const SubmissionsTable: FC<{ submissions: ProjectSubmissionsType[] | null; onChange: (s: ProjectSubmissionsType[]) => void }> = ({ submissions, onChange }) => {
  const { t } = useTranslation()
  const project = useProject()
  const { courseId, projectId } = useParams()
  const { message } = useAppApi()

  const updateTable = async (groupId: number, feedback: Partial<PUT_Requests[ApiRoutes.PROJECT_SCORE]>) => {
    if (!projectId || submissions === null || !groupId) return console.error("No projectId or submissions or groupId found")

    const response = await apiCall.patch(ApiRoutes.PROJECT_SCORE, feedback, { id: projectId, groupId })
    const data = response.data

    const newSubmissions: ProjectSubmissionsType[] = submissions.map((s) => {
      if (s.group.groupId !== groupId) return s
      return {
        ...s,
        feedback: {
          ...s.feedback,
          ...data,
        },
      }
    })

    onChange(newSubmissions)
  }

  const updateScore = async (s: ProjectSubmissionsType, scoreStr: string) => {
    if (!projectId || !project) return console.error("No projectId or project found")
    if (!project.maxScore) return console.error("Scoring not available for this project")
    scoreStr = scoreStr.trim()
    let score: number | null
    if (scoreStr === "") score = null
    else score = parseFloat(scoreStr)
    if (isNaN(score as number)) score = null
    if (score !== null && score > project.maxScore) return message.error(t("project.scoreTooHigh"))
    await updateTable(s.group.groupId, { score })
  }

  const updateFeedback = async (s: ProjectSubmissionsType, feedback: string) => {
    await updateTable(s.group.groupId, { feedback })
  }

  const downloadFile = async (s: ProjectSubmissionsType) => {
    // TODO: implement this
  }

  const columns: TableProps<ProjectSubmissionsType>["columns"] = useMemo(() => {
    const cols: TableProps<ProjectSubmissionsType>["columns"] = [
      {
        title: project?.clusterId ? t("project.group") : t("project.userName"),
        dataIndex: "group",
        key: "group",
        render: (g) => <Typography.Text>{g.name}</Typography.Text>,
        sorter: (a: ProjectSubmissionsType, b: ProjectSubmissionsType) => {
          return a.group.groupId - b.group.groupId
        },
      },
      {
        title: t("project.submission"),
        key: "submissionId",
        render: (s: ProjectSubmissionsType) => (
          <Link
            to={AppRoutes.SUBMISSION.replace(":submissionId", s.submission?.submissionId + "")
              .replace(":projectId", s.submission?.projectId + "")
              .replace(":courseId", courseId!)}
          >
            <Button type="link">#{s.submission?.submissionId}</Button>
          </Link>
        ),
      },
      {
        title: t("project.status"),
        dataIndex: "submission",
        key: "submissionStatus",
        render: (s) => (
          <Typography.Text>
            <SubmissionStatusTag status={createStatusBitVector(s)} />{" "}
          </Typography.Text>
        ),
      },
      {
        title: t("project.submissionTime"),
        dataIndex: "submission",
        key: "submission",
        render: (time: ProjectSubmissionsType["submission"]) => time?.submissionTime && <Typography.Text>{new Date(time.submissionTime).toLocaleString()}</Typography.Text>,
        sorter: (a: ProjectSubmissionsType, b: ProjectSubmissionsType) => {
          // Implement sorting logic for submissionTime column
          const timeA: any = a.submission?.submissionTime || 0;
          const timeB: any = b.submission?.submissionTime || 0;
          return timeA - timeB;
        },
      },
    ]

    if (!project || project.maxScore) {
      cols.push({
        title: `Score (/${project?.maxScore ?? ""})`,
        key: "score",
        render: (s: ProjectSubmissionsType) => (
          <Typography.Text
            type={!s.feedback || !project || s.feedback.score === null || s.feedback.score < project.maxScore! / 2 ? "danger" : undefined}
            editable={{ onChange: (e) => updateScore(s, e), maxLength: 10 }}
          >
            {s.feedback?.score ?? "-"}
          </Typography.Text>
        ),
      })
    }

    cols.push({
      title: "Download",
      key: "download",
      render: (s: ProjectSubmissionsType) => (
        <Button
          onClick={() => downloadFile(s)}
          type="text"
          icon={<DownloadOutlined />}
        />
      ),
      align: "center",
    })

    return cols
  }, [t, project, submissions])
  return (
    <Table
      showSorterTooltip={{mouseEnterDelay: 1}}
      loading={submissions === null}
      dataSource={submissions ?? []}
      locale={{ emptyText: submissions === null ? t("project.loadingSubmissions") : t("project.noSubmissions") }}
      expandable={{
        expandedRowRender: (g) => (
          <>
            <div style={{ marginBottom: "3rem" }}>
              <Typography.Text strong>{t("project.feedback")}:</Typography.Text>
              <br />
              <br />
              <Typography.Paragraph
                editable={{
                  autoSize: { maxRows: 5, minRows: 3 },
                  onChange: (value) => updateFeedback(g, value),
                }}
              >
                {g.feedback?.feedback || "-"}
              </Typography.Paragraph>
            </div>

            {project?.clusterId && (
              <>
                <Typography.Text strong>{t("project.groupMembers")}</Typography.Text>
                <List
                  locale={{ emptyText: t("project.groupEmpty") }}
                  dataSource={g.group.members ?? []}
                  renderItem={GroupMember}
                />
              </>
            )}
          </>
        ),
      }}
      rowKey={(l) => l.group.groupId}
      pagination={false}
      columns={columns}
    />
  )
}

export default SubmissionsTable
