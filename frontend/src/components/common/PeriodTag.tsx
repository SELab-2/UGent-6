import { Tag } from "antd";
import { FC } from "react";


// 
const PeriodTag:FC<{start: string, end:string|null }> = ({start, end}) => {

  return <Tag color="blue">{new Date(start).getFullYear()} - {end ? new Date(end).getFullYear()+1 :(new Date().getFullYear()+1) }</Tag> 
}

export default PeriodTag;