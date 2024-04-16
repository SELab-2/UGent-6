import React, { useState } from 'react';
import { Typography, Switch, Input, Button, Upload } from 'antd';
import {InboxOutlined, LeftOutlined} from '@ant-design/icons';
import useProject from '../../hooks/useProject';
import { useTranslation } from 'react-i18next';
import useApp from '../../hooks/useApp';
import {Link} from "react-router-dom";
import useCourse from "../../hooks/useCourse";

const { Title, Text } = Typography;
const { TextArea } = Input;

// Custom component for code block
const CodeBlock: React.FC<{ value: string; placeholder: string, onChange: (value: string) => void }> = ({ value, placeholder, onChange }) => {
    const [fileContent, setFileContent] = useState<string>('');

    const handleFileUpload = (file: File) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const contents = e.target?.result as string;
            setFileContent(contents);
            onChange(contents);
        };
        reader.readAsText(file);
    };

    return (
        <div style={{ marginTop: '10px', maxHeight: '20em', overflowY: 'auto' }}>
            <TextArea
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                autoSize={{ minRows: 3 }}
                style={{ fontFamily: 'monospace', whiteSpace: 'pre', overflowX: 'auto' }}
            />
            <div style={{ marginTop: '8px', display: 'flex', justifyContent: 'flex-end' }}>
                <Upload accept=".*" showUploadList={false} beforeUpload={handleFileUpload}>
                    <Button icon={<InboxOutlined />}>Upload</Button>
                </Upload>
            </div>
        </div>
    );
};

const ProjectTestsPage: React.FC = () => {
    const app = useApp();
    const [mode, setMode] = useState<string>('Template');
    const [enableFeatureTests, setEnableFeatureTests] = useState<boolean>(false);
    const [enableDockerTests, setEnableDockerTests] = useState<boolean>(false);
    const [structureTemplateText, setStructureTemplateText] = useState<string>('');
    const [dockerImageText, setDockerImageText] = useState<string>('');
    const [dockerScriptText, setDockerScriptText] = useState<string>('');
    const [templateText, setTemplateText] = useState<string>('');
    const project = useProject();
    const { t } = useTranslation();
    const course = useCourse()


    const handleModeChange = (value: string) => {
        setMode(value);
    };

    const handleFeatureTestsChange = (checked: boolean) => {
        setEnableFeatureTests(checked);
    };

    const handleDockerTestsChange = (checked: boolean) => {
        setEnableDockerTests(checked);
    };

    const handleSubmit = () => {
        // Implementeer de logica voor het submitten van de formuliergegevens
        console.log('Formulier ingediend:', { mode, enableFeatureTests, enableDockerTests, structureTemplateText, dockerImageText, dockerScriptText, templateText });
    };

    return (
        <div style={{maxWidth: '80%', margin: '0 auto'}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                <div>
                    <Title level={2}
                           style={{marginBottom: '0.5rem'}}>{t('project.tests.title', {projectName: project?.name})}</Title>
                    <Text type="secondary">{t('project.tests.subtitle')}</Text>
                </div>
                <Link to={`/courses/${course?.courseId}/projects/${project?.projectId}`}>
                    <Button
                        type="default"
                        icon={<LeftOutlined/>}
                        style={{marginLeft: '1rem'}}
                    >
                        {t('project.back')}
                    </Button>
                </Link>
            </div>

            <div style={{marginTop: '20px'}}>
                <Text strong>{t('project.tests.structureSlider')}</Text>
                <Switch checked={enableFeatureTests} onChange={handleFeatureTestsChange}/>
            </div>

            {enableFeatureTests && (
                <>
                    <div style={{marginTop: '20px'}}>
                        <Text strong>{t('project.tests.structureTemplateHeader')}</Text>
                        <TextArea
                            value={structureTemplateText}
                            onChange={(e) => setStructureTemplateText(e.target.value)}
                            placeholder={t('project.tests.structurePlaceholder')}
                            autoSize={{minRows: 3}}
                            style={{marginTop: '8px', fontFamily: 'monospace'}}
                        />
                    </div>
                </>
            )}

            <div style={{marginTop: '20px'}}>
                <Text strong>{t('project.tests.dockerSlider')}</Text>
                <Switch checked={enableDockerTests} onChange={handleDockerTestsChange}/>
            </div>

            {enableDockerTests && (
                <>
                    <div style={{marginTop: '20px'}}>
                        <Text strong>{t('project.tests.dockerImageHeader')}</Text>
                        <Input
                            value={dockerImageText}
                            style={{marginTop: '8px'}}
                            onChange={(e) => setDockerImageText(e.target.value)}
                            placeholder={t('project.tests.dockerImagePlaceholder')}
                        />
                    </div>
                    <div style={{marginTop: '20px'}}>
                        <Text strong>{t('project.tests.dockerScriptHeader')}</Text>
                        <CodeBlock value={dockerScriptText} placeholder={t('project.tests.dockerScriptPlaceholder')}
                                   onChange={setDockerScriptText}/>
                    </div>
                </>
            )}

            <div>
                <Text strong>{t('project.tests.mode')}:</Text>
                <div style={{marginTop: '8px', marginBottom: '8px'}}>
                    <Button type={mode === 'Template' ? 'primary' : 'default'}
                            onClick={() => handleModeChange('Template')}>
                        {t('project.tests.modeTemplate')}
                    </Button>
                    <Button type={mode === 'Simpel' ? 'primary' : 'default'} onClick={() => handleModeChange('Simpel')}
                            style={{marginLeft: '10px'}}>
                        {t('project.tests.modeSimple')}
                    </Button>
                </div>
            </div>

            {mode === 'Template' &&
                <>
                    <Text strong> {t("project.tests.modeHeader")}</Text>
                    <CodeBlock
                        value={templateText} placeholder={t('project.tests.modeTemplatePlaceholder')}
                        onChange={setTemplateText}/>
                </>}

            <div style={{marginTop: '8px'}}>
                <Button type="primary" onClick={handleSubmit}>
                    {t('project.tests.submit')}
                </Button>
            </div>
        </div>
    );
};
export default ProjectTestsPage
