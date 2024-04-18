

import { FC, PropsWithChildren } from "react"
import useCourseUser from "./useCourseUser"



const CourseAdminView:FC<PropsWithChildren> = ({children}) => {
  const relation = useCourseUser().relation
  return relation === "course_admin" || relation === "creator" ?  <>{children}</> : null
}

export default CourseAdminView