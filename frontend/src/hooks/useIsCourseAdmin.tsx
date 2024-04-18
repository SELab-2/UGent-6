import { useContext } from "react"
import { CourseContext } from "../router/CourseRoutes"




const useIsCourseAdmin = () => {
  const relation = useContext(CourseContext).member.relation
  return relation === "course_admin" || relation === "creator"
}

export default useIsCourseAdmin