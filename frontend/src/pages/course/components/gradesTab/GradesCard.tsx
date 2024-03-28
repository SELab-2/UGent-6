import { Card } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../../@types/requests"
import GradesList from "./GradesList"
import useCourse from "../../../../hooks/useCourse"

export type GroupFeedback = GET_Responses[ApiRoutes.PROJECT_SCORE] & {
  project: {
    name: string
    url: string
    projectId: number,
    maxScore:number
  }
  group: {
    groupId: number
    groupName: string
    groupUrl: string
  },
  updatedOn: string
}

const GradesCard = () => {
  const [feedback, setFeedback] = useState<GroupFeedback[] | null>(null)
  const course = useCourse()


  useEffect(() => {
    // TODO: do this fetch, (atm there's no way to get all the grades in a single request, maybe add new api route that gives all the grades of a course)

    setTimeout(() => {
      setFeedback([
        {
      
          group: {
            groupId: 1,
            groupName: "Group 1",
            groupUrl: "/group-1"
          },
          score: 85,
          feedback: "Good job on the project!",
          project: {
            name: "Project 1",
            url: "/project-1",
            projectId: 1,
            maxScore: 100,
          },
          updatedOn: "2021-09-01"
        },
        {
     
          group: {
            groupId: 2,
            groupName: "Group 2",
            groupUrl: "/group-2"
          },
          score: 90,
          feedback: "Excellent work!",
          project: {
            name: "Project 2",
            url: "/project-2",
            projectId: 2,
            maxScore: 100,
          },
          updatedOn: "2025-09-01"
        },
        {
     
          group: {
            groupId: 2,
            groupName: "Group 2",
            groupUrl: "/group-2"
          },
          score: 10,
          feedback: "Bad algo",
          project: {
            name: "Project 2",
            url: "/api/projects/3",
            projectId: 3,
            maxScore: 20
          },
          updatedOn: "2023-09-01"
        },
      ])
    }, 250)
  }, [])
  if (feedback === null) return <Card loading />

  return <Card>
    <GradesList courseId={course.courseId}  feedback={feedback} />
  </Card>
}

export default GradesCard
