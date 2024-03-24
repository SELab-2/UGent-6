import { List, Typography } from "antd"
import { FC } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import { Link } from "react-router-dom"
import SubmissionStatusTag from "./SubmissionStatusTag"

export type SubmissionType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS][number]


const SubmissionList: FC<{ submissions: SubmissionType[] | null }> = ({ submissions }) => {
  const SubmissionItem = (submission: SubmissionType) => {
    return (
      <List.Item actions={[
        <SubmissionStatusTag key="status" docker_accepted={submission.docker_accepted} structure_accepted={submission.structure_accepted} />
      ]}>
        <Link to={"feedback/" + submission.submittionId}>
          <Typography.Link href={"feedback/" + submission.submittionId}>#{submission.submittionId}</Typography.Link>
        </Link>
      </List.Item>
    )
  }

  return (
    <List
      loading={submissions === null}
      dataSource={submissions ?? []}
      renderItem={SubmissionItem}
    />
  )
}

export default SubmissionList
