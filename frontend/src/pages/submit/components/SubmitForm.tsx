import {InboxOutlined} from "@ant-design/icons"
import {Form, FormInstance, Upload} from "antd"
import {FC} from "react"
import {useTranslation} from "react-i18next"


const SubmitForm: FC<{ form: FormInstance, setFileAdded: (added: boolean) => void, onSubmit: (values: any) => void }> = ({form, setFileAdded, onSubmit}) => {

    const {t} = useTranslation()
    const normFile = (e: any) => {
        console.log("Upload event:", e)
        if (Array.isArray(e)) {
            return e
        }
        return e?.fileList
    }

  const onFinish = (values: any) => {
    onSubmit(values);
  };

    return (
    <Form form={form} layout="vertical" style={{height: "100%"}} onFinish={onFinish}>

            <Form.Item
                name={t("project.addFiles")}
                valuePropName="fileList"
                getValueFromEvent={normFile}
                style={{height: "100%"}}
            >
                <Upload.Dragger
                    name="file"
                    multiple={false}
                    style={{height: "100%"}}
                    onChange={({file}) => {
                        if (file.status !== 'uploading') {
                            setFileAdded(true);
                        }
                    }}
                >
                    <p className="ant-upload-drag-icon">
                        <InboxOutlined/>
                    </p>
                    <p className="ant-upload-text">{t("project.uploadAreaTitle")}</p>
                    <p className="ant-upload-hint">{t("project.uploadAreaSubtitle")}</p>
                </Upload.Dragger>
            </Form.Item>
        </Form>
    )
}

export default SubmitForm
