import { TeamOutlined } from "@ant-design/icons"
import { Card, Statistic, theme } from "antd"
import { FC } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"

type CourseType = GET_Responses[ApiRoutes.COURSE]

const Course: FC<{ course: CourseType }> = ({ course }) => {
  const { token } = theme.useToken()
  return (
    <Card
      styles={{
        header: {
          background: token.colorPrimaryBg,
        },
        title: {
          fontSize: "1.1em",
        },
      }}
      bordered={false}
      hoverable
      type="inner"
      title={course.name}
      style={{ width: 300 }}
      actions={[
        <Statistic
          valueStyle={{ fontSize: "1em", color: token.colorTextLabel }}
          prefix={<TeamOutlined />}
          value={72}
        />,
      ]}
    ></Card>
  )
}

export default Course
