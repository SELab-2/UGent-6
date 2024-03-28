import React, { useState } from 'react';
import { useParams, useNavigation } from 'react-router-dom';
import {Button, Form, Input, Switch, DatePicker, theme} from 'antd';
import {useTranslation} from "react-i18next";
import useApp from "../../hooks/useApp";
import useCourse from "../../hooks/useCourse";
import  { ProjectFormData } from './components/ProjectCreateService';
import Error from "../error/Error";
import ProjectCreateService from "./components/ProjectCreateService";


const ProjectCreate: React.FC = () => {
    const [form] = Form.useForm();
    const { token } = theme.useToken()
    const { t } = useTranslation()
    const app = useApp()
    const course = useCourse()
    const { courseId } = useParams()
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const handleCreation = async (values: ProjectFormData) => {
        setLoading(true);
        return true;
    };


    return (
        <Form form={form} onFinish={handleCreation} layout="vertical">
            <Form.Item label={t("project.change.name")} name="name" rules={[{ required: true, message: t("project.change.nameMessage") }]}>
                <Input />
            </Form.Item>
            <Form.Item label={t("project.change.description")} name="description" rules={[{ required: true, message: t("project.change.descriptionMessage") }]}>
                <Input.TextArea />
            </Form.Item>
            <Form.Item label={t("project.change.groupClusterId")} name="groupClusterId" rules={[{ required: true, message: t("project.change.groupClusterIdMessage") }]}>
                <Input type="number" />
            </Form.Item>
            <Form.Item label={t("project.change.testId")} name="testId">
                <Input type="number" />
            </Form.Item>
            <Form.Item label={t("project.change.visible")} name="visible" valuePropName="checked">
                <Input type="checkbox" />
            </Form.Item>
            <Form.Item label={t("project.change.maxScore")} name="maxScore" rules={[{ required: true, message: t("project.change.maxScoreMessage") }]}>
                <Input type="number" />
            </Form.Item>
            <Form.Item label={t("project.change.deadline")} name="deadline">
                <DatePicker showTime format="YYYY-MM-DD HH:mm:ss" />
            </Form.Item>
            <Form.Item>
                <Button type="primary" htmlType="submit">
                    {t("project.change.create")}
                </Button>
            </Form.Item>
        </Form>
    );
};

export default ProjectCreate;

