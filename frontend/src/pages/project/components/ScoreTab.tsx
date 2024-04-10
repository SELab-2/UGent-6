import { Card, Typography } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import apiCall from "../../../util/apiFetch"
import useProject from "../../../hooks/useProject"
import { useParams } from "react-router-dom"

export type ScoreType = GET_Responses[ApiRoutes.PROJECT_SCORE]

const ScoreCard = () => {

  /**
   * undefined -> loading
   * null -> no score available
   * ScoreType -> score available
   */
  const [score, setScore] = useState<ScoreType | null | undefined>(undefined)
  const {projectId} = useParams()
  const project = useProject()
  console.log(project);

  useEffect(() => {
    // /projects/{projectid}/groups/{groupid}/score
    if(!projectId) return console.error("No project id")
    let ignore = false
    apiCall.get(ApiRoutes.PROJECT_SCORE, {id:projectId}).then((response)=> {
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
  if (score === null) return <div>
    <Typography.Text>No score available</Typography.Text>
  </div>
  return (
    <Card
      title="Score"
      extra={[project && <Typography.Text key="score" strong>{score.score} / {project.maxScore}</Typography.Text>]}
    >
    
      <Typography.Text>{score.feedback}</Typography.Text>
    </Card>
  )
}

export default ScoreCard
