import { useContext } from "react"
import { CourseContext } from "../router/CourseRoutes"




const useCourseUser = () => {
  const {member} = useContext(CourseContext)
  return member
}

export default useCourseUser