import { Card, Col, Row, Space, Tooltip, Typography, theme } from "antd"
import useCourse from "../../../../hooks/useCourse"
import MarkdownTextfield from "../../../../components/input/MarkdownTextfield"
import { InfoCircleOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import CourseAdminView from "../../../../hooks/CourseAdminView"
import useCourseUser from "../../../../hooks/useCourseUser"
import { useLocation } from "react-router-dom"
import { useContext, useMemo } from "react"
import openInviteModal from "../tabExtraBtn/InviteModalContent"
import useAppApi from "../../../../hooks/useAppApi"
import { CourseContext } from "../../../../router/CourseRoutes"

class UrlBuilder {
  private baseUrl: string
  private params: Record<string, string | number | boolean>

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
    this.params = {}
  }

  addParam(key: string, value: string | number | boolean) {
    this.params[key] = value
    return this
  }

  build() {
    const url = new URL(this.baseUrl)
    Object.keys(this.params).forEach((key) => {
      if (this.params[key] !== null && this.params[key] !== undefined) {
        url.searchParams.append(key, this.params[key].toString())
      }
    })
    return url.toString()
  }
}

export const generateLink = (courseId:string, joinKey:string|null) => {
  const urlBuilder = new UrlBuilder(`${window.location.origin}/invite/${courseId}`)
  if (joinKey) {
    urlBuilder.addParam("key", joinKey)
  }
  return urlBuilder.build()
}

const InformationTab = () => {
  const course = useCourse()
  const { t } = useTranslation()
  const courseUser = useCourseUser()
  const {modal} = useAppApi()
  const {setCourse} = useContext(CourseContext)
  const {token} = theme.useToken()

  const url = useMemo<string>(() => generateLink(course.courseId.toString(), course.joinKey), [course])

  return (
    <Row gutter={8}>
      <Col span={courseUser.relation === "enrolled" ? 24 : 18}>
        <Card
          style={{ height: "100%" }}
          styles={{
            body: {
              padding: "1rem 2rem",
            },
          }}
        >
          <MarkdownTextfield content={course.description} />
        </Card>
      </Col>
      <CourseAdminView>
        <Col span={6}>
          <Card>
           
            <Typography.Text strong>{t("course.inviteLink")}:  <Tooltip title={t("course.inviteLinkInfo")}>
              <InfoCircleOutlined />
            </Tooltip></Typography.Text> <br />
            <Typography.Link copyable 
            editable={{
              onStart: () => openInviteModal({course, modal, onChange: setCourse, title: t("course.invitePeopleToCourse")}),
            }}
            style={{color:token.colorLink, cursor:"default"}}
            >{url}</Typography.Link>
          </Card>
        </Col>
      </CourseAdminView>
    </Row>
  )
}

export default InformationTab
