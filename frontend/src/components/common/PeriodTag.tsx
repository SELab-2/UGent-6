import { Tag } from "antd";
import { FC } from "react";


// 
const PeriodTag:FC<{year: number }> = ({year}) => {

  return <Tag color="blue">{year} - {year+1}</Tag> 
}

export default PeriodTag;