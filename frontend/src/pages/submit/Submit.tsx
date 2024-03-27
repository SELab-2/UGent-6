import { Affix, Button, Card, Col, Form, Row, Typography } from "antd"
import { useTranslation } from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"

const Submit = () => {
  const { t } = useTranslation()
  const [form] = Form.useForm()
  return (
    <Row
      style={{ margin: "3rem 0" }}
      gutter={[32, 32]}
    >
      <Col md={12} sm={24} xs={24}>
        <Card title={t("project.files")} style={{height:"100%"}}>
          <SubmitForm form={form} />
        </Card>
      </Col>

      <Col md={12} sm={24} xs={24}>
        <Card
          title={t("project.structure")}
          style={{height:"100%"}}
          styles={{ body: { display: "flex", justifyContent: "center" } }}
        >
          <SubmitStructure structure="test" />
        </Card>
      </Col>
{/* 
      <Affix offsetTop={500} onChange={(affixed) => console.log(affixed)}>
    <Button>Indienen</Button>
  </Affix> */}
    </Row>
  )
}

export default Submit
