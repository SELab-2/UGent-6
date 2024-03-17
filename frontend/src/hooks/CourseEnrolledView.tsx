

import { FC, PropsWithChildren } from "react"
import useCourseUser from "./useCourseUser"



const CourseEnrolledView:FC<PropsWithChildren> = ({children}) => {
  const relation = useCourseUser().relation
  return relation === "enrolled" ?  <>{children}</> : null
}

export default CourseEnrolledView