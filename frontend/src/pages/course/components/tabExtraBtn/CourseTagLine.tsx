import { FC } from "react"
import { CourseType } from "../../Course"
import { Space, Tag } from "antd"
import PeriodTag from "../../../../components/common/PeriodTag"
import { InboxOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"

const CourseTagLine: FC<{ course: CourseType }> = ({ course }) => {
  const { t } = useTranslation()

  return (
    <Space
      direction="horizontal"
      size="small"
      style={{ marginBottom: "0.5rem" }}
    >
      <PeriodTag year={course.year} />
      <Tag
        key={course.teacher.url}
        color="gold"
      >
        {course.teacher.name} {course.teacher.surname}
      </Tag>
      {course.archivedAt && (
        <Tag
          icon={<InboxOutlined />}
          color="orange"
        >
          {t("course.archived")}
        </Tag>
      )}
    </Space>
  )
}

export default CourseTagLine
