import { FC, createContext, useEffect, useMemo, useState } from "react"
import { Outlet, useParams } from "react-router-dom"
import { CourseType } from "../pages/course/Course"
import { Flex, Spin } from "antd"
import useUser from "../hooks/useUser"
import { UserCourseType } from "../providers/UserProvider"

export type CourseContextType = {
  course: CourseType
  member: UserCourseType
}
export type CourseMemberType = {
  userId: number
  relation: "enrolled" | "course_admin" | "creator"
  name: string
  surname: string
}

export const CourseContext = createContext<CourseContextType>({} as CourseContextType)

const CourseRoutes: FC = () => {
  const params = useParams<{ id: string }>()
  const [course, setCourse] = useState<CourseType | null>(null)
  const { courses } = useUser()
  const [member, setMember] = useState<UserCourseType | null>(null)

  useEffect(() => {
    if (!courses?.length) return
    const member = courses.find((c) => c.courseId === parseInt(params.id ?? "0"))
    if (!member) return // TODO: handle error
    setMember(member)
  }, [courses])

  useEffect(() => {
    // TODO: fetch course data: /api/courses/1

    // TODO: if user is not in member list -> redirect to dashboard + snackbar message
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

  if (!course || !member)
    return (
      <div style={{ width: "100%", height: "100%", justifyContent: "center", display: "flex", alignItems: "center" }}>
        <Spin size="large"></Spin>
      </div>
    )

  return (
    <CourseContext.Provider value={{ course: course!, member: member! }}>
      <Outlet />
    </CourseContext.Provider>
  )
}

export default CourseRoutes
