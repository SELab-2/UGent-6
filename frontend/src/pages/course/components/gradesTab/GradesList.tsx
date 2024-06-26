import { Button, List, Typography } from "antd"
import { FC } from "react"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../../@types/routes"
import { CourseGradesType } from "./GradesCard"

const GradesList: FC<{ feedback: CourseGradesType[]; courseId: number }> = ({ feedback, courseId }) => {
  const { t } = useTranslation()
  const navigate = useNavigate()

  
  return (
    <>
      <List
      locale={ {emptyText: t("course.noFeedback")} }
        dataSource={feedback.filter(s => s.groupFeedback !== null)}
        header={
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <div>
              <Typography.Text strong>Feedback</Typography.Text>
            </div>

            <div>
              <Typography.Text strong>{t("course.score")}</Typography.Text>
            </div>
          </div>
        }
        renderItem={(score) => (
          <List.Item
            actions={score.maxScore ? [
              <Typography.Text key="score">
                {score.groupFeedback!.score} / {score.maxScore}
              </Typography.Text>,
            ] : []}
          >
            <List.Item.Meta
              title={
                <div>
                  <Button
                    style={{ padding: 0, margin: 0, fontWeight: 600 }}
                    type="link"
                    onClick={() => navigate(AppRoutes.PROJECT.replace(":courseId", courseId.toString()).replace(":projectId", score.projectId.toString()))}
                  >
                    {score.projectName}
                  </Button>
                </div>
              }
              description={score.groupFeedback!.feedback}
              style={{whiteSpace: "pre-wrap"}}
            />
          </List.Item>
        )}
      />
    </>
  )
}

export default GradesList
