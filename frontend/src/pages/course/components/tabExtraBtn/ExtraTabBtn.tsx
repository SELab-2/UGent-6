import { Button } from "antd"
import { useContext } from "react"
import { CourseContext } from "../../../../router/CourseRoutes"
import LeaveCourseButton from "./LeaveCourseButton"
import { useParams } from "react-router-dom"
import CourseAdminBtn from "./CourseAdminBtn"




const ExtraTabBtn = () => {
  const { member } = useContext(CourseContext)
  const { courseId} = useParams()


  if(member.relation === "enrolled") {
    return <LeaveCourseButton courseId={courseId!}/>
  } 
  

  return <CourseAdminBtn courseId={courseId!}/>

  
}

export default ExtraTabBtn