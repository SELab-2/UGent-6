import { Button, List, Table, TableProps, Typography } from "antd"
import { FC, useEffect } from "react"
import { Link, useParams } from "react-router-dom"
import SubmissionStatusTag, { createStatusBitVector } from "./SubmissionStatusTag"
import { GroupSubmissionType } from "./SubmissionTab"
import { useTranslation } from "react-i18next"
import { AppRoutes } from "../../../@types/routes"




const SubmissionList: FC<{ submissions: GroupSubmissionType[] | null, indices: Map<Number, Number> }> = ({ submissions, indices }) => {
  const {t} = useTranslation()
  const {courseId} = useParams()
  
  const columns: TableProps['columns'] = [
    {
      title: t("project.submission"),
      key: "submissionId",
      //sorter: (a: GroupSubmissionType, b: GroupSubmissionType) => {
      //  return a.submissionId - b.submissionId
      //},
      render: (submission: GroupSubmissionType, _, index:number) => (
        <Link to={AppRoutes.SUBMISSION.replace(":courseId",courseId!).replace(":projectId",submission.projectId+"").replace(":submissionId",submission.submissionId+"")}>
          <Button type="link">#{indices ? indices.get(submission.submissionId)?.toString() : ""}</Button>
        </Link>
      ),
    },
    {
      title: t("project.submissionTime"),
      dataIndex: "submissionTime",
      key: "submissionTime",
      sorter: (a: GroupSubmissionType, b: GroupSubmissionType) => new Date(a.submissionTime).getTime() - new Date(b.submissionTime).getTime(),
      render: (submission: GroupSubmissionType["submissionTime"]) => (
        <Typography.Text>{new Date(submission).toLocaleString()}</Typography.Text>
      ),
    },
    {
      title: t("project.status"),
      key:"status",
      render: (submission: GroupSubmissionType) => (
        <SubmissionStatusTag status={createStatusBitVector(submission)} />
      ),
    }
  ]



  return ( <Table locale={{emptyText: t("project.noSubmissions")}}  showSorterTooltip={{mouseEnterDelay: 1}} loading={submissions === null} dataSource={submissions||[]} columns={columns} rowKey="submissionId" />
  )
}

export default SubmissionList
