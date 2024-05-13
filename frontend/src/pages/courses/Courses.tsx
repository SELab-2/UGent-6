import { Button, Card, Flex, Input } from "antd"
import useUser from "../../hooks/useUser"
import CoursesList from "./components/CoursesList"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router-dom"
import { useState } from "react"
import { SortAscendingOutlined, SortDescendingOutlined } from "@ant-design/icons"

const Courses = () => {
  const { courses } = useUser()
  const { t } = useTranslation()
  const location = useLocation()
  const [filter, setFilter] = useState<string>("")
  const [ascendingOrder, setAscendingOrder] = useState<"asc" | "desc">("asc")

  console.log(courses)

  // Get 'role' query string
  let role = new URLSearchParams(location.search).get("role")
  if (role !== "enrolled" && role !== "admin") {
    role = null
  }

  const toggleSortOrder = () => {
    setAscendingOrder(ascendingOrder === "asc" ? "desc" : "asc")
  }

  return (
    <Card
      title={t("courses.courses")}
      style={{ margin: "2rem 0" }}
      extra={
        <Flex gap="0.5rem">
          <Button
            icon={ascendingOrder === "asc" ? <SortAscendingOutlined /> : <SortDescendingOutlined />}
            onClick={toggleSortOrder}
            type="dashed"
          >
            {ascendingOrder === "asc" ? t("courses.sortAscending") : t("courses.sortDescending")}
          </Button>

          <Input.Search
            key="search"
            onChange={(e) => setFilter(e.target.value)}
            placeholder={t("courses.searchCourse")}
          />
        </Flex>
      }
    >
      <CoursesList
        role={role}
        filter={filter}
        courses={courses}
        order={ascendingOrder}
      />
    </Card>
  )
}

export default Courses
