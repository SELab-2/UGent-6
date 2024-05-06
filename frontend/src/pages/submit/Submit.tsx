import {Affix, Button, Card, Col, Form, Row, Typography} from "antd"
import {useTranslation} from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"
import {useNavigate, useParams} from "react-router-dom"
import React, {useState, useRef} from 'react';
import apiCall from "../../util/apiFetch";
import {ApiRoutes} from "../../@types/requests.d";
import JSZip from 'jszip';

const Submit = () => {
    const {t} = useTranslation()
    const [form] = Form.useForm()
    const {projectId} = useParams<{ projectId: string }>()
    const [fileAdded, setFileAdded] = useState(false);
    const navigate = useNavigate()

    const onSubmit = async (values: any) => {
        console.log("Received values of form: ", values)
        const files = values.files.map((file: any) => file.originFileObj);
        if (files.length === 0) {
            console.error("No files selected")
            return
        }
        console.log(files);
        const formData = new FormData()

        const zip = new JSZip();
        files.forEach((file: any) => {
            zip.file(file.name, file);
        });
        const content = await zip.generateAsync({type: "blob"});
        formData.append("file", content, "files.zip");

        if (!projectId) return;
        const response = await apiCall.post(ApiRoutes.PROJECT_SUBMIT, formData, {id: projectId})
        console.log(response)
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
