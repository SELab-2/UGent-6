import { Card, Col, Row, Space, Tooltip, Typography } from "antd"
import useCourse from "../../../../hooks/useCourse"
import MarkdownTextfield from "../../../../components/input/MarkdownTextfield"
import { InfoCircleOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"

const InformationTab = () => {
  const course = useCourse()
  const {t} = useTranslation()

  return (
    <Row gutter={8}>
      <Col span={18}>
        <Card style={{ width: "100%" }}>
          <MarkdownTextfield content={course.description} />
        </Card>
      </Col>
      <Col span={6}>
        <Card>
        <Tooltip title={t("course.inviteLinkInfo")}><InfoCircleOutlined/></Tooltip> <Typography.Text strong>{t("course.inviteLink")}: </Typography.Text>  <br/>
          <Typography.Link copyable>
            {window.location.host + "/invite/" + course.courseId}
          </Typography.Link>
        </Card>
      </Col>
    </Row>
  )
}

export default InformationTab
