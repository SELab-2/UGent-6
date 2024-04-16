import { FC, createContext, useEffect,  useState } from "react"
import { Outlet, useParams } from "react-router-dom"
import { CourseType } from "../pages/course/Course"
import { Flex, Spin } from "antd"
import useUser from "../hooks/useUser"
import { UserCourseType } from "../providers/UserProvider"
import apiCall from "../util/apiFetch"
import { ApiRoutes } from "../@types/requests.d"

export type CourseContextType = {
  course: CourseType
  setCourse: (course: CourseType) => void
  member: UserCourseType
}


export const CourseContext = createContext<CourseContextType>({} as CourseContextType)

const CourseRoutes: FC = () => {
  const { courseId } = useParams<{ courseId: string }>()
  const [course, setCourse] = useState<CourseType | null>(null)
  const [member, setMember] = useState<UserCourseType | null>(null)
  const { courses } = useUser()


  useEffect(() => {
    if (!courses?.length || !course) return
    const member = courses.find((c) => c.courseId === parseInt(courseId ?? "0"))
    if (!member) return console.error("Member not found") // TODO: handle error
    setMember(member)
  }, [courses, course])

  useEffect(() => {
    if (!courseId) return

    let ignore = false

    apiCall
      .get(ApiRoutes.COURSE, { courseId: courseId! })
      .then((res) => {
        // TODO: if user is not in member list -> render 403 page
        if (!ignore) {
          console.log("Course: ", res.data)
          setCourse(res.data)
        }
      })
      .catch((err) => {
        // TODO: handle error
        console.log(err)
      })

    return () => {
      ignore = true
    }
  }, [courseId])

  if (!course || !member)
    return (
      <div style={{ width: "100%", height: "100%", justifyContent: "center", display: "flex", alignItems: "center" }}>
        <Spin size="large" />
      </div>
    )

  return (
    <CourseContext.Provider value={{ setCourse,course: course!, member: member! }}>
      <Outlet />
    </CourseContext.Provider>
  )
}

export default CourseRoutes
