import { Card } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests.d"
import GradesList from "./GradesList"
import useCourse from "../../../../hooks/useCourse"
import useApi from "../../../../hooks/useApi"

export type CourseGradesType = GET_Responses[ApiRoutes.COURSE_GRADES][number]
const GradesCard = () => {
  const [feedback, setFeedback] = useState<CourseGradesType[] | null>(null)
  const course = useCourse()
  const API = useApi()

  useEffect(() => {
    let ignore = false

    API.GET(ApiRoutes.COURSE_GRADES, { pathValues: { id: course.courseId } }, "message").then((res) => {
      if (!ignore && res.success) setFeedback(res.response.data)
    })

    return () => {
      ignore = true
    }
  }, [API])
  if (feedback === null) return <Card loading />

  return (
    <Card>
      <GradesList
        courseId={course.courseId}
        feedback={feedback}
      />
    </Card>
  )
}

export default GradesCard
