import { Card, Spin, theme , Input, Button, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { GET_Responses } from "../../../@types/requests";
import { ApiRoutes } from "../../../@types/requests";
import { ArrowLeftOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { AppRoutes } from "../../../@types/routes";
import '@fontsource/jetbrains-mono';

export type SubmissionType = GET_Responses[ApiRoutes.SUBMISSION]

const SubmissionCard: React.FC<{ submission: SubmissionType }> = ({ submission }) => {
  const { token } = theme.useToken()
  const { t } = useTranslation()

  const downloadSubmission = () => {
    //TODO: file vullen met echte file content
    const fileContent = 'Hello world';
    const blob = new Blob([fileContent], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'indiening.zip';
    document.body.appendChild(link);
    link.click();
    URL.revokeObjectURL(url);
    document.body.removeChild(link);
};

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
          {/*TODO: moet werken met projectId en courseId*/}
          <Link to={AppRoutes.PROJECT.replace(":projectId","...").replace(":courseId","...")}>
            <Button type="text" style={{marginRight: 16}}><ArrowLeftOutlined/></Button>
          </Link>
          {t("submission.submission")}
        </span>
      }
    >
      {t("submission.submittedFiles")}

      <ul style={{ listStyleType: 'none' }}>
        <li>
          <Button type="link" style={{padding: 0}}onClick={downloadSubmission}><u>indiening.zip</u></Button>
        </li>
      </ul>

      {t("submission.structuretest")}

      <ul style={{ listStyleType: 'none' }}>
        <li>
          <Typography.Text type={submission.structure_accepted ? "success" : "danger"}>
            {submission.structure_accepted ? t("submission.status.accepted") : t("submission.status.failed")}
          </Typography.Text>
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
        <li>
          <Typography.Text type={submission.docker_accepted ? "success" : "danger"}>
            {submission.docker_accepted ? t("submission.status.accepted") : t("submission.status.failed")}
          </Typography.Text>
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
