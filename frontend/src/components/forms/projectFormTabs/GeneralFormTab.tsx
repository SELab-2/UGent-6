import { DatePicker, Form, FormInstance, Input, Switch, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { FC, useEffect, useState } from "react"
import MarkdownEditor from "../../input/MarkdownEditor"

const GeneralFormTab: FC<{ form: FormInstance }> = ({ form }) => {
    const { t } = useTranslation()
    const description = Form.useWatch("description", form)
    const visible = Form.useWatch("visible", form)
    const [isVisible, setIsVisible] = useState(visible)
    const [savedVisibleAfter, setSavedVisibleAfter] = useState<string | null>(null)

    useEffect(() => {
        setIsVisible(visible)
        if (visible && savedVisibleAfter) {
            form.setFieldsValue({ visibleAfter: null })
        }
    }, [visible])

    const handleVisibleChange = (checked: boolean) => {
        setIsVisible(checked)
        if (checked) {
            setSavedVisibleAfter(form.getFieldValue("visibleAfter"))
            form.setFieldsValue({ visibleAfter: null })
        } else {
            form.setFieldsValue({ visibleAfter: savedVisibleAfter })
        }
    }

    return (
        <>
            <Form.Item
                label={t("project.change.name")}
                name="name"
                rules={[{ required: true, message: t("project.change.nameMessage") }]}
            >
                <Input />
            </Form.Item>

            <Typography.Text>
                {t("project.change.description")}
            </Typography.Text>
            <MarkdownEditor value={description} maxLength={5000} />

            <Form.Item
                label={t("project.change.visible")}
                required
                name="visible"
                valuePropName="checked"
            >
                <Switch onChange={handleVisibleChange} />
            </Form.Item>

            {!isVisible && (
                <Form.Item
                    label={t("project.change.visibleAfter")}
                    name="visibleAfter"
                >
                    <DatePicker
                        showTime
                        format="YYYY-MM-DD HH:mm:ss"
                    />
                </Form.Item>
            )}

            <Form.Item
                label={t("project.change.maxScore")}
                name="maxScore"
                tooltip={t("project.change.maxScoreHelp")}
                rules={[{ required: false, message: t("project.change.maxScoreMessage") }]}
            >
                <Input
                    min={1}
                    max={1000}
                    type="number"
                />
            </Form.Item>

            <Form.Item
                label={t("project.change.deadline")}
                name="deadline"
                rules={[{ required: true }]}
            >
                <DatePicker
                    showTime
                    format="YYYY-MM-DD HH:mm:ss"
                />
            </Form.Item>
        </>
    )
}

export default GeneralFormTab
