import { FC, PropsWithChildren } from "react"
import useIsTeacher from "./useIsTeacher"


const TeacherView:FC<PropsWithChildren> = ({children}) => {
  const isTeacher=  useIsTeacher()
  return isTeacher ?  <>{children}</> : null
}

export default TeacherView