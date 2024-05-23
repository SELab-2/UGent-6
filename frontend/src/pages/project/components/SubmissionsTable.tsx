import { Button, Input, List, Table, Tooltip, Typography } from "antd";
import { FC, useMemo, useState } from "react";
import { ProjectSubmissionsType } from "./SubmissionsTab";
import { TableProps } from "antd/lib";
import { useTranslation } from "react-i18next";
import {DownloadOutlined, SaveOutlined} from "@ant-design/icons";
import useProject from "../../../hooks/useProject";
import SubmissionStatusTag, { createStatusBitVector } from "./SubmissionStatusTag";
import { Link, useParams } from "react-router-dom";
import { AppRoutes } from "../../../@types/routes";
import { ApiRoutes, PUT_Requests } from "../../../@types/requests.d";
import useAppApi from "../../../hooks/useAppApi";
import useApi from "../../../hooks/useApi";

const GroupMember = ({ name, studentNumber }: ProjectSubmissionsType["group"]["members"][number]) => {
  return (
      <List.Item>
        <List.Item.Meta title={name} description={studentNumber || ""} />
      </List.Item>
  );
};

const SubmissionsTable: FC<{ submissions: ProjectSubmissionsType[] | null; onChange: (s: ProjectSubmissionsType[]) => void; withArtifacts?: boolean }> = ({ submissions, onChange, withArtifacts }) => {
  const { t } = useTranslation();
  const project = useProject();
  const { courseId, projectId } = useParams();
  const { message } = useAppApi();
  const API = useApi();
  const [editingFeedback, setEditingFeedback] = useState<{ [key: number]: string }>({});
  const [isEditing, setIsEditing] = useState<{ [key: number]: boolean }>({});

  const updateTable = async (groupId: number, feedback: Omit<PUT_Requests[ApiRoutes.PROJECT_SCORE], "projectId" | "groupId">, usePost: boolean) => {
    if (!projectId || submissions === null || !groupId) return console.error("No projectId or submissions or groupId found");

    let res;
    if (usePost) {
      res = await API.POST(
          ApiRoutes.PROJECT_SCORE,
          {
            body: feedback,
            pathValues: { id: projectId, groupId },
          },
          "message"
      );
    } else {
      res = await API.PUT(ApiRoutes.PROJECT_SCORE, { body: feedback, pathValues: { id: projectId, groupId } }, "message");
    }
    if (!res.success) return;

    const data = res.response.data;

    const newSubmissions: ProjectSubmissionsType[] = submissions.map((s) => {
      if (s.group.groupId !== groupId) return s;
      return {
        ...s,
        feedback: {
          ...s.feedback,
          ...data,
        },
      };
    });

    onChange(newSubmissions);
  };

  const updateScore = async (s: ProjectSubmissionsType, scoreStr: string) => {
    if (!projectId || !project) return console.error("No projectId or project found");
    if (!project.maxScore) return console.error("Scoring not available for this project");
    scoreStr = scoreStr.trim();
    let score: number | null;
    if (scoreStr === "") score = null;
    else score = parseFloat(scoreStr);
    if (isNaN(score as number)) score = null;
    if (score !== null && score > project.maxScore) return message.error(t("project.scoreTooHigh"));
    await updateTable(s.group.groupId, { score: score ?? null, feedback: s.feedback?.feedback ?? "" }, s.feedback === null);
  };

  const updateFeedback = async (groupId: number) => {
    const feedback = editingFeedback[groupId];
    if (feedback !== undefined) {
      const s = submissions?.find(s => s.group.groupId === groupId)
      await updateTable(groupId, { feedback, score: s?.feedback?.score ?? null }, s?.feedback === null);
      setEditingFeedback((prev) => {
        const newState = { ...prev };
        delete newState[groupId];
        return newState;
      });
      setIsEditing((prev) => ({ ...prev, [groupId]: false }));
    }
  };

  const downloadFile = async (route: ApiRoutes.SUBMISSION_FILE | ApiRoutes.SUBMISSION_ARTIFACT, filename: string) => {
    const response = await API.GET(
        route,
        {
          config: {
            responseType: "blob",
            transformResponse: [(data) => data],
          },
        },
        "message"
    );
    if (!response.success) return;
    const url = window.URL.createObjectURL(new Blob([response.response.data]));
    const link = document.createElement("a");
    link.href = url;
    let fileName = filename + ".zip"; // default filename
    link.setAttribute("download", fileName);
    document.body.appendChild(link);
    link.click();
    link.parentNode!.removeChild(link);
  };

  const downloadSubmission = async (submission: ProjectSubmissionsType) => {
    if (!submission.submission) return console.error("No submission found");
    downloadFile(submission.submission.fileUrl, submission.group.name + ".zip");
    if (withArtifacts && submission.submission.artifactUrl) {
      downloadFile(submission.submission.artifactUrl, submission.group.name + "-artifacts.zip");
    }
  };

  const handleEditFeedback = (groupId: number) => {
    setIsEditing((prev) => ({ ...prev, [groupId]: true }));
  };

  const columns: TableProps<ProjectSubmissionsType>["columns"] = useMemo(() => {
    const cols: TableProps<ProjectSubmissionsType>["columns"] = [
      {
        title: project?.clusterId ? t("project.group") : t("project.userName"),
        dataIndex: "group",
        key: "group",
        render: (g) => <Typography.Text>{g.name}</Typography.Text>,
        sorter: (a: ProjectSubmissionsType, b: ProjectSubmissionsType) => {
          return a.group.groupId - b.group.groupId;
        },
      },
      {
        title: t("project.submission"),
        key: "submissionId",
        render: (s: ProjectSubmissionsType) =>
            s.submission ? (
                <Link
                    to={AppRoutes.SUBMISSION.replace(":submissionId", s.submission?.submissionId + "")
                        .replace(":projectId", s.submission?.projectId + "")
                        .replace(":courseId", courseId!)}
                >
                  <Button type="link">#{s.submission?.submissionId}</Button>
                </Link>
            ) : null,
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
      },
    ];

    if (!project || project.maxScore) {
      cols.push({
        title: `Score (/${project?.maxScore ?? ""})`,
        key: "score",
        render: (s: ProjectSubmissionsType) => (
            <Typography.Text
                type={!s.feedback || s.feedback.score === null ? "secondary" : !project || s.feedback.score < project.maxScore! / 2 ? "danger" : undefined}
                editable={{ onChange: (e) => updateScore(s, e), maxLength: 10 }}
            >
              {s.feedback?.score ?? t("project.noScoreLabel")}
            </Typography.Text>
        ),
      });
    }

    cols.push({
      title: "Download",
      key: "download",
      render: (s: ProjectSubmissionsType) => (
          <Tooltip title={s.submission ? "" : t("project.noSubmissionDownload")}>
            <Button onClick={() => downloadSubmission(s)} disabled={!s.submission} type="text" icon={<DownloadOutlined />} />
          </Tooltip>
      ),
      align: "center",
    });

    return cols;
  }, [t, project, submissions]);

  return (
      <Table
          showSorterTooltip={{ mouseEnterDelay: 1 }}
          loading={submissions === null}
          dataSource={submissions ?? []}
          locale={{ emptyText: submissions === null ? t("project.loadingSubmissions") : t("project.noSubmissions") }}
          expandable={{
            expandedRowRender: (g) => (
                <>
                  <div style={{marginBottom: "3rem"}}>
                    <Typography.Text strong>{t("project.feedback")}:</Typography.Text>
                    <br/>
                    <br/>
                    {isEditing[g.group.groupId] ? (
                        <>
                          <Input.TextArea
                              autoSize={{ minRows: 3, maxRows: 5 }}
                              value={editingFeedback[g.group.groupId] ?? g.feedback?.feedback ?? ""}
                              onChange={(e) =>
                                  setEditingFeedback((prev) => ({
                                    ...prev,
                                    [g.group.groupId]: e.target.value,
                                  }))
                              }
                          />

                          <Button
                            style={{float: "right", marginTop: "0.5rem"}}
                            onClick={() => updateFeedback(g.group.groupId)}
                            color="primary"
                            icon={<SaveOutlined/>}>
                          </Button>
                        </>
                    ) : (
                        <>
                           <Typography.Paragraph
                              style={{
                                whiteSpace: "pre-wrap",
                                cursor: "pointer",
                              }}
                              editable={true}
                              onClick={() => handleEditFeedback(g.group.groupId)}
                          >
                            {g.feedback?.feedback ?? t("project.noFeedbackLabel")}
                            </Typography.Paragraph>
                        </>
                    )}
                  </div>


                  {project?.clusterId && (
                      <>
                        <Typography.Text strong>{t("project.groupMembers")}</Typography.Text>
                        <List locale={{emptyText: t("project.groupEmpty")}} dataSource={g.group.members ?? []}
                              renderItem={GroupMember}/>
                      </>
                  )}
                </>
            ),
          }}
          rowKey={(l) => l.group.groupId}
          pagination={false}
          columns={columns}
      />
  );
};

export default SubmissionsTable;
