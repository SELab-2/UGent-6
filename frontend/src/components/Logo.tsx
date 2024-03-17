import { Typography } from "antd"
import { TitleProps } from "antd/es/typography/Title"
import { FC } from "react"
import { Link } from "react-router-dom"



const Logo:FC<TitleProps> = (props) => {



  return <Typography.Title {...props} level={3}>
    <Link to="/" style={{color:"inherit"}}>Pigeonhole</Link>
</Typography.Title>
}

export default Logo