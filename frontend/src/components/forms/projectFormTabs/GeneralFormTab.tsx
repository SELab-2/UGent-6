import { DatePicker, Form, FormInstance, Input, Switch, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { FC } from "react"
import MarkdownEditor from "../../input/MarkdownEditor"
import dayjs from 'dayjs';

const GeneralFormTab: FC<{ form: FormInstance }> = ({ form }) => {
    const { t } = useTranslation()
    const description = Form.useWatch("description", form)
    const visible = Form.useWatch("visible", form)

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
                <Switch />
            </Form.Item>

            {!visible && (
                <Form.Item
                    label={t("project.change.visibleAfter")}
                    tooltip={t("project.change.visibleAfterTooltip")}
                    name="visibleAfter"
                >
                    <DatePicker
                        showTime
                        format="YYYY-MM-DD HH:mm:ss"
                        allowClear={true}
                        disabledDate={(current) => current && current.isBefore(dayjs().startOf('day'))}
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
                    showTime={{
                        format: "HH:mm:ss",
                        disabledHours: () => {
                          const hours = [];
                          for (let i = 0; i < dayjs().hour(); i++) {
                            hours.push(i);
                          }
                          return hours;
                        },
                        disabledMinutes: (selectedHour) => {
                          const minutes = [];
                          if (selectedHour === dayjs().hour()) {
                            for (let i = 0; i < dayjs().minute(); i++) {
                              minutes.push(i);
                            }
                          }
                          return minutes;
                        },
                        disabledSeconds: (selectedHour, selectedMinute) => {
                          const seconds = [];
                          if (selectedHour === dayjs().hour() && selectedMinute === dayjs().minute()) {
                            for (let i = 0; i < dayjs().second(); i++) {
                              seconds.push(i);
                            }
                          }
                          return seconds;
                        },
                      }}
                    format="YYYY-MM-DD HH:mm:ss"
                    disabledDate={(current) => current && current.isBefore(dayjs().startOf('day'))}

                />
            </Form.Item>
        </>
    )
}

export default GeneralFormTab
