import { Button, List, Typography } from "antd"
import { FC } from "react"
import { GroupFeedback } from "./GradesCard"
import { useTranslation } from "react-i18next"
import { Link } from "react-router-dom"
import { AppRoutes } from "../../../../@types/routes"

const GradesList: FC<{ feedback: GroupFeedback[], courseId:number }> = ({ feedback,courseId }) => {
  const { t } = useTranslation()
  

  return (
    <>
      <List
        dataSource={feedback}
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
            actions={[
              <Typography.Text>
                {score.score} / {score.maxScore}
              </Typography.Text>,
            ]}
          >
            <List.Item.Meta
              title={<div>
                <Link to={AppRoutes.PROJECT.replace(":courseId",courseId.toString()).replace(":projectId",score.project.projectId.toString())}>
                <Typography.Link  >
                {score.project.name}
              </Typography.Link>

                  
                </Link>
                </div>}
              description={score.feedback}
            />
          </List.Item>
        )}
      />
    </>
  )
}

export default GradesList
