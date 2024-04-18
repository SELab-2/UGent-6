import { Card } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import GradesList from "./GradesList"
import useCourse from "../../../../hooks/useCourse"
import apiCall from "../../../../util/apiFetch"

export type CourseGradesType = GET_Responses[ApiRoutes.COURSE_GRADES][number]
const GradesCard = () => {
  const [feedback, setFeedback] = useState<CourseGradesType[] | null>(null)
  const course = useCourse()


  useEffect(() => {
    // TODO: do this fetch, (atm there's no way to get all the grades in a single request, maybe add new api route that gives all the grades of a course)
    
    apiCall.get(ApiRoutes.COURSE_GRADES, { id: course.courseId }).then((res) => {
      console.log(res.data);
      setFeedback(res.data)
    })
    
  }, [])
  if (feedback === null) return <Card loading />

  return <Card>
    <GradesList courseId={course.courseId}  feedback={feedback} />
  </Card>
}

export default GradesCard
