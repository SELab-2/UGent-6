import { Affix, Button, Card, Col, Form, Row, Typography } from "antd"
import { useTranslation } from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"
import { useNavigate } from "react-router-dom"

const Submit = () => {
  const { t } = useTranslation()
  const [form] = Form.useForm()

  const navigate = useNavigate()


  return (
    <>
      <div>
        <Row
          style={{ margin: "3rem 0" }}
          gutter={[32, 32]}
        >
          <Col
            md={16}
            sm={24}
            xs={24}
          >
            <Card
              title={t("project.files")}
              style={{ height: "100%" }}
              styles={{ body: { height:"100%" } }}
            >
              <SubmitForm form={form} />
            </Card>
          </Col>

          <Col
            md={8}
            sm={24}
            xs={24}
          >
            <Card
              title={t("project.structure")}
              style={{ height: "100%" }}
              styles={{ body: { display: "flex", justifyContent: "center" } }}
            >
              <SubmitStructure structure="test" />
            </Card>
          </Col>
        </Row>
      </div>
      <Card
        style={{ position: "fixed", bottom: 32, width: "calc(100% - 8rem)", maxWidth: "1200px", height: "4rem", margin: "1rem" }}
        styles={{ body: { padding:"10px 0",display:"flex",gap:"1rem" } }}
      >
        <Button size="large" onClick={() => navigate(-1)}>
          {t("goBack")}
        </Button>
        <Button
          type="primary"
          size="large"
          style={{ width: "100%", height: "100%" }}
          disabled={!form.isFieldsTouched()}
          onClick={()=> form.submit()}
        >
          {t("project.submit")}
        </Button>
      </Card>
    </>
  )
}

export default Submit
