import {  TeamOutlined } from "@ant-design/icons"
import { Card, Statistic, theme } from "antd"
import { FC } from "react"



const Course:FC = () => {
  const {token} =  theme.useToken()
  return <Card styles={{
    header: {
      background:token.colorPrimaryBg,
    },
    title:{
      fontSize:"1.1em"
    }
  }} bordered={false} hoverable type="inner" title="Computationele biologie" style={{ width: 300 }} actions={[
    <Statistic valueStyle={{fontSize:"1em",color: token.colorTextLabel}} prefix={<TeamOutlined />} value={72}  />
  ]}> 

  </Card>
}

export default Course