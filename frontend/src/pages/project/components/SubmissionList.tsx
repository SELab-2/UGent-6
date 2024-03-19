import { List } from "antd"
import { useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"


export type SubmissionType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS]

const SubmissionList = () => {

  const [submissions, setSubmissions] = useState<SubmissionType[]|null>(null)



  return (
    <List>

    </List>
  )
}

export default SubmissionList