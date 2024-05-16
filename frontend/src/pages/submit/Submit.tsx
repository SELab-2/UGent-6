import {Button, Card, Col, Form, Row} from "antd"
import {useTranslation} from "react-i18next"
import SubmitForm from "./components/SubmitForm"
import SubmitStructure from "./components/SubmitStructure"
import {useNavigate, useParams} from "react-router-dom"
import {useState} from 'react';
import apiCall from "../../util/apiFetch";
import {ApiRoutes} from "../../@types/requests.d";
import JSZip from 'jszip';
import {AppRoutes} from "../../@types/routes";
import useAppApi from "../../hooks/useAppApi"

const Submit = () => {
    const {t} = useTranslation()
    const [form] = Form.useForm()
    const {projectId, courseId} = useParams<{ projectId: string, courseId: string}>()
    const {message} = useAppApi()
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
            zip.file(file.webkitRelativePath || file.name, file);
        });
        const content = await zip.generateAsync({type: "blob"});
        formData.append("file", content, "files.zip");

        if (!projectId) return;
        const response = await apiCall.post(ApiRoutes.PROJECT_SUBMIT, formData, {id: projectId})
        console.log(response)
        const submissionId:string =  response.data.submissionId.toString();
        if (response.status === 200) { // Check if the submission was successful
            message.success(t("project.submitSuccess"));
        }
        else {
            message.error(t("project.submitError"));
        }
        if (courseId != null && submissionId != null) {
            navigate(AppRoutes.SUBMISSION.replace(':courseId', courseId).replace(':projectId', projectId).replace(':submissionId', submissionId));
        }else{
            console.log(projectId)
            console.log(courseId)
            console.log(submissionId)
            message.error(t("project.submitError"));
        }
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