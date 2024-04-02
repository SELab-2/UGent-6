import { DownloadOutlined } from "@ant-design/icons"
import { Button, Card, Tooltip } from "antd"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import SubmissionsTable from "./SubmissionsTable"

export type ProjectSubmissionsType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS][number]


// Card of all the latests submissions for a project
const SubmissionsCard = () => {
  const { t } = useTranslation()
  const [submissions, setSubmissions] = useState<ProjectSubmissionsType[] | null>(null)

  useEffect(() => {
    // TODO: make request to /projects/{projectid}/submissions

    setTimeout(() => {
      setSubmissions([
        {
          docker_accepted: true,
          docker_feedback: "",
          file_url: "/api/submissions/1/file",
          group_url: "/api/groups/1",
          group: {
            groupId: 1,
            members: [{
              name: "Bard",
              surname: "Carter",
              url: "/api/groups/1/members/1",
              userId:1
            },{
              name: "Bob",
              surname: "Carter",
              url: "/api/groups/1/members/2",
              userId:2
            }],
            name: "Groep 1",
          },
          project_url: "/api/projects/1",
          projectId: 1,
          courseId: 1,
          structure_accepted: true,
          structure_feedback: "",
          submitted_time: "2024-01-01T00:00:00.000Z",
          submissionId: 1,
          feedback: {
            feedback: null,
            score:null
          }
        }
      ])
    }, 250)
  }, [])

  const handleDownloadSubmissions = () => {}

  return (
    <Card
      title={t("project.submissions")}
      extra={
        <Tooltip title={t("project.downloadSubmissions")}>
          <Button onClick={handleDownloadSubmissions} icon={<DownloadOutlined />} />
        </Tooltip>
      }
      styles={{
        body: {padding: 0}
      }}
    >
      <SubmissionsTable submissions={submissions} />

    </Card>
  )
}

export default SubmissionsCard
