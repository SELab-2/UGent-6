import { useState } from "react"; // Import useState hook
import { Collapse, Flex, Input, Space, Spin, Tag, Typography, Checkbox } from "antd";
import { useTranslation } from "react-i18next";
import { SubTest } from "../../../@types/requests";
import { FC } from "react";
import { SubmissionType } from "./SubmissionCard";
import { CheckCircleOutlined, CloseCircleOutlined } from "@ant-design/icons";
import MarkdownTextfield from "../../../components/input/MarkdownTextfield";
import { CheckboxChangeEvent } from "antd/es/checkbox";

const SubmissionContent: FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { t } = useTranslation();

  let subtest_length = 1;

  if (submission.dockerFeedback?.type === "TEMPLATE") {
    subtest_length = submission.dockerFeedback.feedback.subtests.length;
  }

  const [showParsedContent, setShowParsedContent] = useState<boolean[]>(Array(subtest_length).fill(false));

  const TestResults: React.FC<SubTest[]> = (subTests) => {
    
    const handleCheckboxChange = (index: number) => (e: CheckboxChangeEvent) => {
      setShowParsedContent(prevState => {
        const newState = [...prevState];
        newState[index] = e.target.checked;
        return newState;
      });
    };
  
    return (<Collapse style={{ marginTop: 8 }}>
      {subTests.map((test, index) => {
        const successText = test.succes ? t("submission.success") : t("submission.failed");
        const successType = test.succes ? "success" : "danger";
        return (
          <Collapse.Panel
            key={index}
            header={
              <>
                <Typography.Text type={successType}>{`${test.testName}: ${successText}`}</Typography.Text>{" "}
                {!test.required && (
                  <Typography.Text style={{ marginLeft: "0.5rem" }} type="secondary">
                    ({t("submission.optional")})
                  </Typography.Text>
                )}
              </>
            }
          >
            {!!test.testDescription?.length && <Typography.Paragraph type="secondary">{test.testDescription}</Typography.Paragraph>}
            <div style={{ marginBottom: "1rem" }}>
              <Checkbox checked={showParsedContent[index]} onChange={handleCheckboxChange(index)}>
                <div style= {{marginTop: "2px"}}>{t("submission.viewraw")}</div>
              </Checkbox>
          </div>
            <Flex justify="space-around" gap="1rem">
              <div style={{ width: "100%" }}>
                <Typography.Title level={5} style={{ marginTop: "0.5rem" }}>
                  {t("submission.expected")}
                </Typography.Title>
                <Input.TextArea
                  autoSize={{ minRows: 3, maxRows: 20 }}
                  readOnly
                  value={showParsedContent[index] ? JSON.stringify(test.correct) : test.correct}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono" }}
                  variant="borderless"
                />
              </div>
              <div style={{ width: "100%" }}>
                <Typography.Title level={5} style={{ marginTop: "0.5rem" }}>
                  {t("submission.received")}
                </Typography.Title>
                <Input.TextArea
                  autoSize={{ minRows: 3, maxRows: 20 }}
                  readOnly
                  value={showParsedContent[index] ? JSON.stringify(test.output) : test.output}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono" }}
                  variant="borderless"
                />
              </div>
            </Flex>
          </Collapse.Panel>
        );
      })}
    </Collapse>
  )};



  if (submission.dockerStatus === "aborted") return <Typography.Text type="danger">{t("submission.dockertestAborted")}</Typography.Text>;

  return (
    <>
      <>
        <Typography.Text strong>{t("submission.structuretest")}</Typography.Text>
        <li>
          {submission.structureAccepted ? (
            <Typography.Text type="success">{t("submission.structureTestSuccess")}</Typography.Text>
          ) : (
            <>
              <Typography.Text type="danger">{t("submission.structureTestFailed")}</Typography.Text>
              <MarkdownTextfield content={submission.structureFeedback} />
            </>
          )}
        </li>
      </>

      {submission.dockerStatus === "no_test" && submission.structureAccepted && (
        <Typography.Text style={{ marginTop: "1rem" }} type="success">
          {t("submission.submissionSuccess")}
        </Typography.Text>
      )}

      {submission.dockerStatus === "running" && (
        <div style={{ textAlign: "center" }}>
          <br />
          <Spin size="large" />
          <br />
          <br />
          <Typography.Text type="secondary">{t("submission.running")}</Typography.Text>
          <br />
          <br />
        </div>
      )}

      {submission.dockerStatus === "finished" && (
        <div style={{ marginTop: "1rem" }}>
          <Typography.Text strong>{t("submission.tests")}:</Typography.Text>
          <br />
          <>
            <Typography.Text type={submission.dockerFeedback.allowed ? "success" : "danger"}>
              {submission.dockerFeedback.allowed ? t("submission.status.accepted") : t("submission.status.failed")}
            </Typography.Text>
            {submission.dockerFeedback.type === "SIMPLE" ? (
              <div>
                <Input.TextArea
                  readOnly
                  value={submission.dockerFeedback.feedback}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono", marginTop: 8 }}
                  autoSize={{ minRows: 4, maxRows: 128 }}
                />
              </div>
            ) : submission.dockerFeedback.type === "TEMPLATE" ? (
              TestResults(submission.dockerFeedback.feedback.subtests)
            ) : null}
          </>
        </div>
      )}
    </>
  );
};

export default SubmissionContent;
