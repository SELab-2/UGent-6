import {Affix, Button, Card, Col, Form, Row, Typography} from "antd"
import {useTranslation} from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"
import {useNavigate, useParams} from "react-router-dom"
import React, {useState, useRef} from 'react';
import apiCall from "../../util/apiFetch";
import {ApiRoutes} from "../../@types/requests.d";

const Submit = () => {
    const {t} = useTranslation()
    const [form] = Form.useForm()
    const {projectId} = useParams<{ projectId: string }>()
    const [fileAdded, setFileAdded] = useState(false);
    const navigate = useNavigate()

const onSubmit = async (values: any) => {
  console.log("Received values of form: ", values)
  const file = values[t("project.addFiles")][0].originFileObj
  if (!file) {
    console.error("No file selected")
    return
  }
  const formData = new FormData()
  formData.append("file", file)
  if (!projectId) return;
  const response = await apiCall.post(ApiRoutes.PROJECT_SUBMIT, formData, {id: projectId})
}
    return (
        <>
            <div>
                <Row
                    style={{marginTop: "3rem"}}
                    gutter={[32, 32]}
                >
                    <Col
                        md={16}
                        sm={24}
                        xs={24}
                    >
                        <Card
                            title={t("project.files")}
                            style={{height: "100%"}}
                            styles={{body: {height: "100%"}}}
                        >
                            <SubmitForm form={form} setFileAdded={setFileAdded} onSubmit={onSubmit}/> </Card>
                    </Col>

                    <Col
                        md={8}
                        sm={24}
                        xs={24}
                    >
                        <Card
                            title={t("project.structure")}
                            style={{height: "100%"}}
                            styles={{body: {display: "flex", justifyContent: "center"}}}
                        >
                            <SubmitStructure structure="test"/>
                        </Card>
                    </Col>
                </Row>
                <Row>
                    <Card
                        style={{width: "100%", maxWidth: "1200px", height: "4rem", margin: "1rem"}}
                        styles={{body: {padding: "10px 0", display: "flex", gap: "1rem"}}}
                    >
                        <Button size="large" onClick={() => navigate(-1)}>
                            {t("goBack")}
                        </Button>
                        <Button
                            type="primary"
                            size="large"
                            style={{width: "100%", height: "100%"}}
                            disabled={!fileAdded}
                            onClick={() => form.submit()}
                        >
                            {t("project.submit")}
                        </Button>
                    </Card>
                </Row>
            </div>

        </>
    )
}

export default Submit
