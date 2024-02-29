import { Typography } from "antd"
import { TitleProps } from "antd/es/typography/Title"
import { FC } from "react"



const Logo:FC<TitleProps> = (props) => {



  return <Typography.Title {...props} level={3}>Pidgeon</Typography.Title>
}

export default Logo