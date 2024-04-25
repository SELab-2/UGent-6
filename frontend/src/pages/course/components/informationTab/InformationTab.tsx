import { Card, Col, Row, Space, Tooltip, Typography } from "antd"
import useCourse from "../../../../hooks/useCourse"
import MarkdownTextfield from "../../../../components/input/MarkdownTextfield"
import { InfoCircleOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import CourseAdminView from "../../../../hooks/CourseAdminView"
import useCourseUser from "../../../../hooks/useCourseUser"

const InformationTab = () => {
  const course = useCourse()
  const { t } = useTranslation()
  const courseUser = useCourseUser()

  return (
    <Row gutter={8}>
      <Col span={courseUser.relation === "enrolled" ? 24 : 18}>
        <Card
          style={{ height: "100%" }}
          styles={{
            body: {
              padding: "0 2rem",
            },
          }}
        >
          <MarkdownTextfield content={course.description} />
        </Card>
      </Col>
      <CourseAdminView>
        <Col span={6}>
          <Card>
            <Tooltip title={t("course.inviteLinkInfo")}>
              <InfoCircleOutlined />
            </Tooltip>{" "}
            <Typography.Text strong>{t("course.inviteLink")}: </Typography.Text> <br />
            <Typography.Link copyable>{window.location.host + "/invite/" + course.courseId}</Typography.Link>
          </Card>
        </Col>
      </CourseAdminView>
    </Row>
  )
}

export default InformationTab
