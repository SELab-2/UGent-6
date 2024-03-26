import { Card, Spin, theme , Input } from "antd"
import { useTranslation } from "react-i18next"
import { GET_Responses } from "../../../@types/requests";
import { ApiRoutes } from "../../../@types/requests";
import { ArrowLeftOutlined } from '@ant-design/icons';
import '@fontsource/jetbrains-mono';

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { token } = theme.useToken()
  const { t } = useTranslation()

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
          <span onClick={() => window.location.href = submission.project_url}>
            <ArrowLeftOutlined style={{ marginRight: 8, cursor: 'pointer' }}/>
          </span>
          {t("submission.submission")}
        </span>
      }
    >
      {t("submission.submittedFiles")}

      <ul style={{ listStyleType: 'none' }}>
        <li>
          <a href={submission.file_url}><u>indiening.zip</u></a>
        </li>
      </ul>

      {t("submission.structuretest")}

      <ul style={{ listStyleType: 'none' }}>
        <li style={{ color: submission.structure_accepted ? '#67d765' : '#da4e4e' }}>
          {submission.structure_accepted ? t("submission.status.accepted") : t("submission.status.failed")}
          {submission.structure_accepted ? null : <div>
            <Input.TextArea
              readOnly
              value={submission.structure_feedback}
              style={{ width: '100%', overflowX: 'auto', overflowY: 'auto', resize: 'none', fontFamily: 'Jetbrains Mono', marginTop: 8 }}
              rows={4}
              autoSize={{ minRows: 4, maxRows: 8 }}
            />
          </div>}
        </li>
      </ul>

      {t("submission.dockertest")}

      {submission.docker_results_available ?
      <ul style={{ listStyleType: 'none' }}>
        <li style={{ color: submission.docker_accepted ? '#67d765' : '#da4e4e' }}>
          {submission.docker_accepted ? t("submission.status.accepted") : t("submission.status.failed")}
          {submission.docker_accepted ? null : <div>
            <Input.TextArea
              readOnly
              value={submission.docker_feedback}
              style={{ width: '100%', overflowX: 'auto', overflowY: 'auto', resize: 'none', fontFamily: 'Jetbrains Mono', marginTop: 8 }}
              rows={4}
              autoSize={{ minRows: 4, maxRows: 8 }}
            />
          </div>}
        </li>
      </ul>

      : <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin
          tip="Loading..."
          size="large"
        />
      </div>}
    </Card>
  )
}

export default SubmissionCard
