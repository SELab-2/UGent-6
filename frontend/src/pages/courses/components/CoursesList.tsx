import { Button, List, Tag } from "antd"
import { FC, useMemo, useState } from "react"
import { UserCourseType } from "../../../providers/UserProvider"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { useTranslation } from "react-i18next"
import { InboxOutlined, TeamOutlined } from "@ant-design/icons"
import PeriodTag from "../../../components/common/PeriodTag"

const CoursesList: FC<{ courses: UserCourseType[] | null; role?: "enrolled" | "admin" | null; filter?: string, order?: "asc" | "desc" }> = ({ courses, role, filter,order }) => {
  const { t } = useTranslation()

  const filteredList = useMemo(() => {
    if (!courses) return courses
    return courses.filter((c) => {
      let r = true
      if (role) {
        if (role === "enrolled") r = role === c.relation
        else r = c.relation === "course_admin" || c.relation === "creator"
      }

      if (filter) {
        r = r && c.name.toLowerCase().includes(filter.toLowerCase())
      }
      return r
    })
  }, [courses, role, filter])

  // Sort the filtered list based on the name of the course
  const sortedList = useMemo(() => {
    if (!filteredList) return filteredList
    return [...filteredList].sort((a, b) => {
      if (order === 'asc') {
        return a.name.localeCompare(b.name)
      } else {
        return b.name.localeCompare(a.name)
      }
    })
  }, [filteredList, order])

  return (
    <div>
      <List
        locale={{ emptyText: t("courses.noCourses") }}
        dataSource={sortedList ?? []}
        loading={sortedList === null}
        renderItem={(course) => (
          <List.Item>
            <List.Item.Meta
              title={
                <Link to={AppRoutes.COURSE.replace(":courseId", course.courseId + "")}>
                  <Button type="link">{course.name}</Button>
                </Link>
              }
            />
            {course.archivedAt && (
                <Tag
                icon={<InboxOutlined />}
                color="orange"
                >
                {t("courses.archived")}
              </Tag>
            )}
            <Tag color="cyan" icon={<TeamOutlined />}>
              {course.memberCount} {t("courses.member" + (course.memberCount === 1 ? "" : "s"))}
            </Tag>


            <PeriodTag year={course.year} />
          </List.Item>
        )}
      />
    </div>
  )
}

export default CoursesList
