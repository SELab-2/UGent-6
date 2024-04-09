import { FC, useEffect, useState } from "react"
import SubmissionList, { SubmissionType } from "./SubmissionList"
import { Button, Card } from "antd"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { PlusOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"

const SubmissionCard: FC<{ projectId: number; courseId: number, allowNewSubmission?:boolean }> = ({ projectId, courseId,allowNewSubmission }) => {
  const navigate = useNavigate()
  const [submissions, setSubmissions] = useState<SubmissionType[] | null>(null)
  const { t } = useTranslation()

  useEffect(() => {
    //TODO: fetch submissions
    setTimeout(() => {
      const submissions: SubmissionType[] = [
        {
          docker_results_available: true,
          docker_accepted: true,
          docker_feedback: "",
          file_url: "/api/submissions/1/file",
          groupUrl: "/api/groups/1",
          project_url: "/api/projects/1",
          structure_accepted: true,
          structure_feedback: "",
          submissionId: 1,
          submitted_time: "2024-01-01T00:00:00.000Z",
          feedback: {
            feedback: "Wow, that's a great submission!",
            score:20
          },
          

          group: {
            name:"Groep 1",
            groupId: 1,
            members: [{
              name:"Bard",
              surname:"Jansen",
              url:"/api/groups/1/members/1",
              userId: 4
            },
            {
              name:"Bard1",
              surname:"Jansen",
              url:"/api/groups/1/members/1",
              userId: 5
            },
            {
              name:"Bard2",
              surname:"Jansen",
              url:"/api/groups/1/members/1",
              userId: 6
            }],
            
          }
        },
        {
          docker_results_available: true,
          docker_accepted: false,
          docker_feedback: "Failed test case 1",
          file_url: "/api/submissions/2/file",
          groupUrl: "/api/groups/1",
          project_url: "/api/projects/1",
          structure_accepted: true,
          structure_feedback: "",
          submissionId: 2,
          submitted_time: "2024-01-01T00:00:00.000Z",
          feedback: {
            feedback: null,
            score:null
          },
          group: {
            name:"Groep 1",
            groupId: 1,
            members: [
              {
                name:"Piet",
                surname:"Jansen",
                url:"/api/groups/1/members/1",
                userId: 1
              },
              {
                name:"Piet1",
                surname:"Jansen",
                url:"/api/groups/1/members/1",
                userId: 2
              },
              {
                name:"Piet2",
                surname:"Jansen",
                url:"/api/groups/1/members/1",
                userId: 3
              }
            ],
            
          }

        },
      ]

      setSubmissions(submissions.slice(0,5)) // Limit to 5 submissions 
    }, 250)
  }, [])

  const handleNewSubmission = () => {
    navigate(AppRoutes.NEW_SUBMISSION.replace(AppRoutes.PROJECT + "/", ""))
  }

  return (
    <Card
      loading={!submissions}
      styles={{
        body:{
          padding:"8px 16px"
        }
      }}
      extra={
        <Button
          disabled={!submissions || allowNewSubmission === false}
          type="primary"
          onClick={handleNewSubmission}
          icon={<PlusOutlined />}
        >
          {t("project.newSubmission")}
        </Button>
      }
      title={t("project.submissions")}
    >
      <SubmissionList submissions={submissions} />
    </Card>
  )
}

export default SubmissionCard
