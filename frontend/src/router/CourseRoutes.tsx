import { FC, createContext, useEffect, useMemo, useState } from "react"
import { Outlet, useParams } from "react-router-dom"
import { CourseType } from "../pages/course/Course"
import { Spin } from "antd"

export type CourseContextType = {
  course: CourseType
}

export const CourseContext = createContext<CourseContextType>({} as CourseContextType)

const CourseRoutes: FC = () => {
  const params = useParams<{ id: string }>()
  const [course, setCourse] = useState<CourseType | null>(null)

  useEffect(() => {
    // TODO: fetch course data

    setTimeout(() => {
      setCourse({
        members_url: "/api/courses/1/members",
        name: "Computationele biologie",
        description: "Een cursus over computationele biologie",
        id: 1,
        teachers: [],
      })
    }, 250)
  }, [params.id])

  if (!course)
    return (
      <div style={{ width: "100%", height: "100%", justifyContent: "center", display: "flex", alignItems: "center" }}>
        <Spin size="large"></Spin>
      </div>
    )

  return (
    <CourseContext.Provider value={{ course: course! }}>
      <Outlet />
    </CourseContext.Provider>
  )
}

export default CourseRoutes
