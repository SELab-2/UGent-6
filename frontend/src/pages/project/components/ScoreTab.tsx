import { Card, Typography } from "antd"
import { useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import apiCall from "../../../util/apiFetch"

export type ScoreType = GET_Responses[ApiRoutes.PROJECT_SCORE]

const ScoreCard = () => {
  const [score, setScore] = useState<ScoreType | null>(null)

  useEffect(() => {
    // /projects/{projectid}/groups/{groupid}/score
    let ignore = false
    apiCall.get(ApiRoutes.PROJECT_SCORE).then((response)=> {
      if (ignore) return
      setScore( response.data)

    })

    return () => {
      ignore = true
    }
  }, [])

  // don't show the card if no score is available
  if (!score) return null

  return (
    <Card
      title="Score"
      extra={[<Typography.Text key="score" strong>{score.score} / {score.maxScore}</Typography.Text>]}
    >
    
      <Typography.Text>{score.feedback}</Typography.Text>
    </Card>
  )
}

export default ScoreCard
