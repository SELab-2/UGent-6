import {Card, Spin, theme, Input, Button, Typography} from "antd"
import {useTranslation} from "react-i18next"
import {GET_Responses} from "../../../@types/requests"
import {ApiRoutes} from "../../../@types/requests"
import {ArrowLeftOutlined} from "@ant-design/icons"
import {useNavigate} from "react-router-dom"
import "@fontsource/jetbrains-mono"
import {useEffect, useState} from "react"
import apiCall from "../../../util/apiFetch"

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({submission}) => {
    const {token} = theme.useToken()
    const {t} = useTranslation()
    const [structureFeedback, setStructureFeedback] = useState<string | null>(null)
    const [dockerFeedback, setDockerFeedback] = useState<string | null>(null)
    const navigate = useNavigate()
    useEffect(() => {
        if (!submission.dockerAccepted) apiCall.get(submission.dockerFeedbackUrl).then((res) => setDockerFeedback(res.data ? res.data : ""))
        if (!submission.structureAccepted) apiCall.get(submission.structureFeedbackUrl).then((res) => setStructureFeedback(res.data ? res.data : ""))
    }, [submission.dockerFeedbackUrl, submission.structureFeedbackUrl])

    const downloadSubmission = async () => {
        try {
            const response = await apiCall.get(submission.fileUrl, undefined, undefined, {
                responseType: 'blob',
                transformResponse: [(data) => data],
            });
            console.log(response);
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            const contentDisposition = response.headers['content-disposition'];
            console.log(contentDisposition);
            let fileName = 'file.zip'; // default filename
            if (contentDisposition) {
                const fileNameMatch = contentDisposition.match(/filename=([^;]+)/);
                console.log(fileNameMatch);
                if (fileNameMatch && fileNameMatch[1]) {
                    fileName = fileNameMatch[1]; // use the filename from the headers
                }
            }
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
        } catch (err) {
            console.error(err);
        }
    }

    return (
        <Card
            styles={{
                header: {
                    background: token.colorPrimaryBg,
                },
                title: {
                    fontSize: "1.1em",
                },
            }}
            type="inner"
            title={
                <span>
          <Button
              onClick={() => navigate(-1)}
              type="text"
              style={{marginRight: 16}}
          >
            <ArrowLeftOutlined/>
          </Button>
                    {t("submission.submission")}
        </span>
            }
        >
            {t("submission.submittedFiles")}

            <ul style={{listStyleType: "none"}}>
                <li>
                    <Button
                        type="link"
                        style={{padding: 0}}
                        onClick={downloadSubmission}
                    >
                        <u>indiening.zip</u>
                    </Button>
                </li>
            </ul>

            {t("submission.structuretest")}

            <ul style={{listStyleType: "none"}}>
                <li>
                    <Typography.Text
                        type={submission.structureAccepted ? "success" : "danger"}>{submission.structureAccepted ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
                    {submission.structureAccepted ? null : (
                        <div>
                            {structureFeedback === null ? (
                                <Spin/>
                            ) : (
                                <Input.TextArea
                                    readOnly
                                    value={structureFeedback}
                                    style={{
                                        width: "100%",
                                        overflowX: "auto",
                                        overflowY: "auto",
                                        resize: "none",
                                        fontFamily: "Jetbrains Mono",
                                        marginTop: 8
                                    }}
                                    rows={4}
                                    autoSize={{minRows: 4, maxRows: 8}}
                                />
                            )}
                        </div>
                    )}
                </li>
            </ul>

            {t("submission.dockertest")}

            <ul style={{listStyleType: "none"}}>
                <li>
                    <Typography.Text
                        type={submission.dockerAccepted ? "success" : "danger"}>{submission.dockerAccepted ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
                    {submission.dockerAccepted ? null : (
                        <div>
                            {dockerFeedback === null ? (
                                <Spin/>
                            ) : (
                                <Input.TextArea
                                    readOnly
                                    value={dockerFeedback}
                                    style={{
                                        width: "100%",
                                        overflowX: "auto",
                                        overflowY: "auto",
                                        resize: "none",
                                        fontFamily: "Jetbrains Mono",
                                        marginTop: 8
                                    }}
                                    rows={4}
                                    autoSize={{minRows: 4, maxRows: 16}}
                                />
                            )}
                        </div>
                    )}
                </li>
            </ul>
        </Card>
    )
}

export default SubmissionCard
