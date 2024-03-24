import { Card, Col, Row, Typography } from "antd"
import { useTranslation } from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"

const Submit = () => {
  const { t } = useTranslation()

  return (
    <Row
      style={{ margin: "3rem 0" }}
      gutter={[32, 32]}
    >
      <Col md={12} sm={24} xs={24}>
        <Card title={t("project.newSubmission")} style={{height:"100%"}}>
          <SubmitForm />
        </Card>
      </Col>

      <Col md={12} sm={24} xs={24}>
        <Card
          title={t("project.structure")}
          style={{height:"100%"}}
          styles={{ body: { display: "flex", justifyContent: "center" } }}
        >
          <Typography.Title level={3}>{}</Typography.Title>

          <SubmitStructure structure="test" />
        </Card>
      </Col>
    </Row>
  )
}

export default Submit
