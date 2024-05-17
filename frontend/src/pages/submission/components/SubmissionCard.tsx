import {Card, Spin, theme, Input, Button, Typography} from "antd"
import {useTranslation} from "react-i18next"
import {GET_Responses} from "../../../@types/requests"
import {ApiRoutes} from "../../../@types/requests"
import {ArrowLeftOutlined} from "@ant-design/icons"
import {useNavigate} from "react-router-dom"
import "@fontsource/jetbrains-mono"
import apiCall from "../../../util/apiFetch"
import {Collapse} from "antd"
import { SubTest } from "../../../@types/requests"
import { useEffect, useState } from "react"

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({submission}) => {
    const {token} = theme.useToken()
    const {t} = useTranslation()
    const navigate = useNavigate()
    const [filename, setFilename] = useState<String | null>(null)

    useEffect(() => {
        const getFileName = async () => {
            //mss is er een betere manier om de filename te krijgen, dit werkt maar kan mss langzaam zijn met hele grote files/directories
            try {
                const response = await apiCall.get(submission.fileUrl, undefined, undefined, {
                    responseType: 'blob',
                    transformResponse: [(data) => data],
                });
                const contentDisposition = response.headers['content-disposition'];
                if (contentDisposition) {
                    const fileNameMatch = contentDisposition.match(/filename=([^;]+)/);
                    console.log(fileNameMatch);
                    if (fileNameMatch && fileNameMatch[1]) {
                        setFilename(fileNameMatch[1]); // use the filename from the headers
                    }
                }
            } catch (err) {
                console.log(err);
                setFilename(null);
            }
        };
    
        getFileName();
    }, [submission]);

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

    const tmpSubTests = [{testName: "test1", testDescription: "test1", succes: true, correct: "correct", output: "correct"}, {testName: "test2", testDescription: "test2", succes: false, correct: "correct", output: "incorrect"}]

    const TestResults: React.FC<SubTest[]> = ( subTests ) => (
        <Collapse style={{marginTop: 8}}>
            {tmpSubTests.map((test, index) => {
            const successText = test.succes ? t("submission.success") : t("submission.failed");
            const successType = test.succes ? 'success' : 'danger';
            return (
                <Collapse.Panel
                    key={index}
                    header={<Typography.Text type={successType}>{`${test.testName}: ${successText}`}</Typography.Text>}
                >
                    <Typography.Paragraph>{test.testDescription}</Typography.Paragraph>
                    <Typography.Title level={5}>Expected Output:</Typography.Title>
                    <Typography.Text>{test.correct}</Typography.Text>
                    <Typography.Title level={5}>Actual Output:</Typography.Title>
                    <Typography.Text>{test.output}</Typography.Text>
                </Collapse.Panel>
            );
        })}
        </Collapse>
    );

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
                        {filename === null ? <Spin></Spin> : <u>{filename}</u>}
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
              {submission.structureFeedback === null ? (
                <Spin />
              ) : (
                <Input.TextArea
                  readOnly
                  value={submission.structureFeedback}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono", marginTop: 8 }}
                  rows={4}
                  autoSize={{ minRows: 4, maxRows: 8 }}
                />
              )}
            </div>
          )}
                </li>
            </ul>

            {submission.dockerStatus !== "running" && submission.dockerFeedback.type === "NONE" ? null : (<>

            {t("submission.dockertest")}

            <ul style={{listStyleType: "none"}}>
                <li>
                    {submission.dockerStatus === "running" ? (
                        <Spin/>
                    ) : (submission.dockerStatus === "aborted" ? t("submission.dockertestAborted") : <>
                    <Typography.Text
                        type={submission.dockerFeedback.allowed ? "success" : "danger"}>{submission.dockerFeedback.allowed ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
                    {submission.dockerFeedback.type === "SIMPLE" ? (
                    <div>
                        <Input.TextArea
                        readOnly
                        value={submission.dockerFeedback.feedback}
                        style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono", marginTop: 8 }}
                        rows={4}
                        autoSize={{ minRows: 4, maxRows: 16 }}
                        />
                    </div>
                    ) : (submission.dockerFeedback.type === "TEMPLATE" ? (
                        TestResults(submission.dockerFeedback.feedback.subtests)
                    ) : null)}
                    </>)}
                </li>
            </ul>
            </>)}
        </Card>
    )
}

export default SubmissionCard
