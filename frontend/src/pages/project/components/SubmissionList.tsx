import { Button, List, Table, TableProps, Typography } from "antd"
import { FC } from "react"
import { Link } from "react-router-dom"
import SubmissionStatusTag, { createStatusBitVector } from "./SubmissionStatusTag"
import { GroupSubmissionType } from "./SubmissionTab"
import { useTranslation } from "react-i18next"




const SubmissionList: FC<{ submissions: GroupSubmissionType[] | null }> = ({ submissions }) => {
  const {t} = useTranslation()

  

  const columns: TableProps['columns'] = [
    {
      title: t("project.submission"),
      dataIndex: "submissionId",
      key: "submissionId",
      render: (submissionId: GroupSubmissionType["submissionId"]) => (
        <Link to={"feedback/" + submissionId}>
          <Button type="link">#{submissionId}</Button>
        </Link>
      ),
    },
    {
      title: t("project.submissionTime"),
      dataIndex: "submissionTime",
      key: "submissionTime",
      
      render: (submission: GroupSubmissionType["submissionTime"]) => (
        <Typography.Text>{new Date(submission).toLocaleString()}</Typography.Text>
      ),
    },
    {
      title: t("project.status"),
      render: (submission: GroupSubmissionType) => (
        <SubmissionStatusTag status={createStatusBitVector(submission)} />
      ),
    }
  ]



  return ( <Table loading={submissions === null} dataSource={submissions||[]} columns={columns} rowKey="submissionId" />
  )
}

export default SubmissionList
