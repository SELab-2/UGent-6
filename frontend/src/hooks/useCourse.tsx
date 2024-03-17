import { useContext } from "react"
import { CourseContext } from "../router/CourseRoutes"




const useCourse = () => {
  const course = useContext(CourseContext)
  return course
}

export default useCourse