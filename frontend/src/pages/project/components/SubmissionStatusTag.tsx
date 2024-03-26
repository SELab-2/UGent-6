import { Tag } from "antd"
import { FC } from "react"



const SubmissionStatusTag:FC<{docker_accepted:boolean, structure_accepted:boolean}> = ({ docker_accepted,structure_accepted }) => {

  if(!docker_accepted){
    return (
      <Tag color="red">Tests failed</Tag>
    )
  } else if (!structure_accepted) {
    return (
      <Tag color="red">Structure Failed</Tag>
    )
  } 

  return <Tag color="green">Passed</Tag>
}

export default SubmissionStatusTag