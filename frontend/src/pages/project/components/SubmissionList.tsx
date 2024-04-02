import { Button, List, Typography } from "antd"
import { FC } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests"
import { Link } from "react-router-dom"
import SubmissionStatusTag from "./SubmissionStatusTag"

export type SubmissionType = GET_Responses[ApiRoutes.PROJECT_SUBMISSIONS][number]

const SubmissionList: FC<{ submissions: SubmissionType[] | null }> = ({ submissions }) => {
  const SubmissionItem = (submission: SubmissionType) => {
    return (
      <List.Item
        actions={[
          <SubmissionStatusTag
            key="status"
            docker_accepted={submission.docker_accepted}
            structure_accepted={submission.structure_accepted}
          />,
        ]}
      >
        <List.Item.Meta
          title={
            <Link to={"feedback/" + submission.submissionId}>
                <Button type="link" size="small" >#{submission.submissionId}</Button>
            </Link>
          }
        />
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
