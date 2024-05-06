import { Card, Typography } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import apiCall from "../../../util/apiFetch"
import useProject from "../../../hooks/useProject"
import { useParams } from "react-router-dom"
import { useTranslation } from "react-i18next"

export type ScoreType = GET_Responses[ApiRoutes.PROJECT_SCORE]

const ScoreCard = () => {

  /**
   * undefined -> loading
   * null -> no score available
   * ScoreType -> score available
   */
  const [score, setScore] = useState<ScoreType | null | undefined>(undefined)
  const {projectId} = useParams()
  const {t} = useTranslation()
  const project = useProject()

  
  useEffect(() => {
    // /projects/{projectid}/groups/{groupid}/score
    if(!projectId) return console.error("No project id")
    if(project?.groupId === undefined) return setScore(null) // Means you aren't in a group yet

    let ignore = false
    apiCall.get(ApiRoutes.PROJECT_SCORE, {id:projectId,groupId: project?.groupId!}).then((response)=> {
      console.log(response.data);
      if (ignore) return
      setScore( response.data)

    }).catch(err => {
      if (ignore) return
      console.log(err);
      setScore(null)
    })

    return () => {
      ignore = true
    }
  }, [])

  // don't show the card if no score is available
  if (score === undefined) return null
  if (score === null) return <div style={{textAlign:"center"}}>
    <Typography.Text type="secondary">{t("project.noScore")}</Typography.Text>
  </div>
  return (
    <Card
      title="Score"
      extra={[project && !!project.maxScore && <Typography.Text key="score" strong>{score.score} / {project.maxScore}</Typography.Text>]}
    
    >
    
      {score.feedback?.length ? <Typography.Text >{score.feedback}</Typography.Text> : <Typography.Text type="secondary">({t("project.noFeedback")})</Typography.Text>}
    </Card>
  )
}

export default ScoreCard
