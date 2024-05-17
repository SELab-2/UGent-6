import { Collapse, Flex, Input, Spin, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { SubTest } from "../../../@types/requests"
import { FC } from "react"
import { SubmissionType } from "./SubmissionCard"

const SubmissionContent: FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { t } = useTranslation()

  const TestResults: React.FC<SubTest[]> = (subTests) => (
    <Collapse style={{ marginTop: 8 }}>
      {subTests.map((test, index) => {
        const successText = test.succes ? t("submission.success") : t("submission.failed")
        const successType = test.succes ? "success" : "danger"
        return (
          <Collapse.Panel
            key={index}
            header={<Typography.Text type={successType}>{`${test.testName}: ${successText}`}</Typography.Text>}
          >
            <Typography.Paragraph type="secondary">{test.testDescription}</Typography.Paragraph>
            <Flex justify="space-around" gap="1rem">
              <div style={{width:"100%"}}>
                <Typography.Title level={5}>{t("submission.expected")}</Typography.Title>
                <Input.TextArea autoSize={{ minRows: 3, maxRows: 20 }} readOnly value={test.correct} style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono" }} />
              </div>
              <div style={{width:"100%"}}>
                <Typography.Title level={5}>{t("submission.received")}</Typography.Title>
                <Input.TextArea autoSize={{ minRows: 3, maxRows: 20 }} readOnly value={test.output} style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono" }} />

              </div>
            </Flex>
          </Collapse.Panel>
        )
      })}
    </Collapse>
  )
  if (submission.dockerStatus === "aborted") return <Typography.Text type="danger">{t("submission.dockertestAborted")}</Typography.Text>
  if (submission.dockerStatus === "running")
    return (
      <div style={{ textAlign: "center" }}>
        <br />
        <Spin size="large" />
        <br />
        <br />
        <Typography.Text type="secondary">{t("submission.running")}</Typography.Text>
        <br />
        <br />
      </div>
    )
  return (
    <>
      {t("submission.structuretest")}

      {submission.dockerStatus === "no_test" && <ul style={{ listStyleType: "none" }}>
        <li>
          <Typography.Text type={submission.structureAccepted ? "success" : "danger"}>{submission.structureAccepted ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
          {submission.structureAccepted ? null : (
            <div>
              {submission.structureFeedback === null ? (
                <Spin />
              ) : (
                <Input.TextArea
                  readOnly
                  value={submission.structureFeedback}
                  style={{ width: "100%", overflowX: "auto", overflowY: "auto", resize: "none", fontFamily: "Jetbrains Mono", marginTop: 8 }}
                  autoSize={{ minRows: 4, maxRows: 128 }}
                />
              )}
            </div>
          )}
        </li>
      </ul>}

      {submission.dockerStatus === "finished" && (
        <ul style={{ listStyleType: "none" }}>
          <li>
            <>
              <Typography.Text type={submission.dockerFeedback.allowed ? "success" : "danger"}>{submission.dockerFeedback.allowed ? t("submission.status.accepted") : t("submission.status.failed")}</Typography.Text>
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
          </li>
        </ul>
      )}
    </>
  )
}

export default SubmissionContent
