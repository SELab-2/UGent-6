import { Card, Input } from "antd"
import useUser from "../../hooks/useUser"
import CoursesList from "./components/CoursesList"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router-dom"
import { useState } from "react"

const Courses = () => {
  const { courses } = useUser()
  const { t } = useTranslation()
  const location = useLocation()
  const [filter,setFilter] = useState<string>("")

  console.log(courses);

  // Get 'role' query string
  let role = new URLSearchParams(location.search).get("role")
  if (role !== "enrolled" && role !== "admin") {
    role = null
  }

  return (
    <Card
      title={t("courses.courses")}
      style={{ margin: "2rem 0" }}
      extra={
        <Input.Search
          key="search"
          onChange={(e)=> setFilter(e.target.value)}
          placeholder={t("courses.searchCourse")}
        />
      }
    >
      <CoursesList
        role={role}
        filter={filter}
        courses={courses}
      />
    </Card>
  )
}

export default Courses
