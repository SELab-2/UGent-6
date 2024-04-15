import React, { useState } from 'react';
import {useParams, useNavigate, useNavigation} from 'react-router-dom';
import {Button, Form, Input, Switch, DatePicker, theme, Checkbox, Typography} from 'antd';
import {useTranslation} from "react-i18next";
import useApp from "../../hooks/useApp";
import useCourse from "../../hooks/useCourse";
import  { ProjectFormData,ProjectError } from './components/ProjectCreateService';
import Error from "../error/Error";
import ProjectCreateService from "./components/ProjectCreateService";
import GroupClusterDropdown from "./components/GroupClusterDropdown";


const ProjectCreate: React.FC = () => {
    const { Title } = Typography;
    const [form] = Form.useForm();
    const { token } = theme.useToken()
    const { t } = useTranslation()
    const app = useApp()
    const course = useCourse()
    const navigate = useNavigate();
    const { courseId } = useParams<{ courseId: string }>();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<ProjectError | null>(null); // Gebruik ProjectError type voor error state
    const [clusterChosen, setClusterChosen] = useState<boolean>(false);
    const [formData, setFormData] = useState<ProjectFormData>({
        name: '',
        description: '',
        groupClusterId: 0,
        testId: null,
        visible: false, // Stel de standaardwaarde in op false
        maxScore: 0,
        deadline: null,
    });
    const handleCreation = async () => {
        setLoading(true);
        try {
            if (courseId !== undefined) {
                // Roep createProject aan en controleer op fouten
                const result = await ProjectCreateService.createProject(courseId, formData);
                if (result instanceof Object && 'code' in result) { // Controleer of result een ProjectError object is
                    setError(result); // Sla de fout op in de state
                } else {
                    navigate(`/courses/${courseId}`)
                }
            } else {
            console.error('courseId is undefined');
            }
        } catch (error:any) {
            // Vang netwerkfouten op
            setError({
                code: 500, // Interne serverfoutcode
                message: error.message || 'Unknown error occurred'
            });
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (fieldName: keyof ProjectFormData, value: any) => {
        if(fieldName === "groupClusterId"){
            setClusterChosen(true);
        }
        setFormData(prevState => ({
            ...prevState,
            [fieldName]: value
        }));
    };


    return (
        <>
            {error && <Error errorCode={error.code} errorMessage={error.message} />} {/* Toon Error-pagina als er een fout is */}
            <Title>
                {t("project.change.title")}
            </Title>
            <Form form={form} onFinish={handleCreation} layout="vertical">
                <Form.Item label={t("project.change.name")} name="name" rules={[{ required: true, message: t("project.change.nameMessage") }]}>
                    <Input value={formData.name} onChange={(e) => handleInputChange('name', e.target.value)} />
                </Form.Item>
                <Form.Item label={t("project.change.description")} name="description" rules={[{ required: true, message: t("project.change.descriptionMessage") }]}>
                    <Input.TextArea value={formData.description} onChange={(e) => handleInputChange('description', e.target.value)} />
                </Form.Item>
                <Form.Item
                    label={t("project.change.groupClusterId")}
                    name="groupClusterId"
                    rules={[{
                        required: !clusterChosen, // Alleen verplicht als er geen cluster is gekozen
                        message: t("project.change.groupClusterIdMessage")
                    }]}
                >
                    <GroupClusterDropdown courseId={courseId || ''} onSelect={(value) => {
                        handleInputChange('groupClusterId', value);
                    }} />
                </Form.Item>
                <Form.Item label={t("project.change.testId")} name="testId">
                    <Input type="number" value={formData.testId || -1} onChange={(e) => handleInputChange('testId', e.target.value)} />
                </Form.Item>
                <Form.Item label={t("project.change.visible")} name="visible" valuePropName="checked">
                    <Checkbox checked={formData.visible} onChange={(e) => handleInputChange('visible', e.target.checked)} />
                </Form.Item>
                <Form.Item label={t("project.change.maxScore")} name="maxScore" rules={[{ required: true, message: t("project.change.maxScoreMessage") }]}>
                    <Input type="number" value={formData.maxScore} onChange={(e) => handleInputChange('maxScore', e.target.value)} />
                </Form.Item>
                <Form.Item label={t("project.change.deadline")} name="deadline" rules={[{required: true}]}>
                    <DatePicker showTime format="YYYY-MM-DD HH:mm:ss" value={formData.deadline} onChange={(date) => handleInputChange('deadline', date)} />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" loading={loading}>
                        {t("project.change.create")}
                    </Button>
                </Form.Item>
            </Form>
        </>
    );
};

export default ProjectCreate;

