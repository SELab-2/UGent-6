import { Button, Input, Modal, Space, Switch, Tooltip, Typography, theme, message } from "antd"
import { FC, useMemo, useState } from "react"
import { generateLink } from "../informationTab/InformationTab"
import { CourseType } from "../../Course"
import { HookAPI } from "antd/es/modal/useModal"
import { InfoCircleOutlined, RedoOutlined, CopyOutlined, CheckOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import useApi from "../../../../hooks/useApi"
import { ApiRoutes } from "../../../../@types/requests.d"

const InviteModalContent: FC<{ defaultCourse: CourseType; onChange: (course: CourseType) => void }> = ({ defaultCourse, onChange }) => {
  const { t } = useTranslation()
  const { token } = theme.useToken()
  const [course, setCourse] = useState<CourseType>(defaultCourse)
  const API = useApi()
  const [loading, setLoading] = useState(false)
  const url = useMemo<string>(() => generateLink(course.courseId.toString(), course.joinKey), [course])
  const [copied, setCopied] = useState(false)

  const regenerateKey = async () => {
    setLoading(true)
    const req = await API.PUT(ApiRoutes.COURSE_JOIN_LINK, { body: undefined, pathValues: { courseId: course.courseId } }, "message")
    setLoading(false)
    if (!req.success) return
    const newCourse = {...course, joinKey: req.response.data}
    onChange(newCourse)
    setCourse(newCourse)
  }

  const toggleJoinKey = async () => {
    if (course.joinKey) {
      setLoading(true)
      const req = await API.DELETE(ApiRoutes.COURSE_JOIN_LINK, { pathValues: { courseId: course.courseId } }, "message")
      setLoading(false)
      if (!req.success) return
      const newCourse = {...course, joinKey: req.response.data}
      onChange(newCourse)
      setCourse(newCourse)
    } else {
      await regenerateKey()
    }
  }

  return (
    <Space
      direction="vertical"
      style={{ width: "100%" }}
      size="large"
    >
      <Space.Compact style={{ width: "100%" }}>
        <Input
          style={{ width: "100%", borderColor: token.colorPrimary }}
          readOnly
          value={url}
          suffix={
            <Tooltip title={copied ? t("course.copyLinkSuccess") : t("course.copyLink")}>
                {copied ? (
                  <CheckOutlined style={{ color: "green" }} />
                ) : (
                  <CopyOutlined onClick={() => { 
                    navigator.clipboard.writeText(url);
                    setCopied(true);
                    setTimeout(() => setCopied(false), 3000);  // Reset after 3 seconds
                  }} />
                )}
              </Tooltip>
          }
        />
        {course.joinKey && (
          <Tooltip title={t("course.regenerateKey")}>
            <Button
              onClick={regenerateKey}
              icon={<RedoOutlined />}
              loading={loading}
            />
          </Tooltip>
        )}
      </Space.Compact>
      <div>
        <Typography.Text>{t("course.allowedInviteText")}</Typography.Text>
        <Switch
          style={{ float: "right" }}
          value={!!course.joinKey}
          loading={loading}
          onChange={toggleJoinKey}
        />
      </div>
    </Space>
  )
}

const openInviteModal = (o: { modal: HookAPI; course: CourseType; title: string; onChange: (course: CourseType) => void }) => {
  o.modal.info({
    title: o.title,
    content: (
      <InviteModalContent
      defaultCourse={o.course}
        onChange={o.onChange}
      />
    ),
    width: 600,
  })
}

export default openInviteModal
