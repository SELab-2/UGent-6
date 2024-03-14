import { Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import Course from "./components/Course"


const Home = () => {
  const { t } = useTranslation()

  return (
    <div style={{margin:"0 2rem"}}>
      
        <Typography.Title level={3}>{t("home.yourCourses")}</Typography.Title>

      <Space>
        <Course />

      </Space>
      
    </div>
  )
}

export default Home
