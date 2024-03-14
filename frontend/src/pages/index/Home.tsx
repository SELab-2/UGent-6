import { Button, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import Course from "./components/Course"
import { PlusOutlined } from "@ant-design/icons"
import TeacherView from "../../hooks/TeacherView"
import CreateCourseModal from "./components/CreateCourseModal"
import { useState } from "react"
import ProjectCard from "./components/ProjectCard"

const Home = () => {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)

  return (<div>
   <div >
      <Typography.Title level={3} style={{
    paddingLeft:"2rem"
   }}>
        {t("home.yourCourses")}
        {/* <TeacherView> */}{" "}
        <Button
        onClick={() => setOpen(true)}
          type="text"
          icon={<PlusOutlined />}
        />
        {/* </TeacherView> */}
      </Typography.Title>

      <Space className="small-scroll-bar" style={{maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap",padding:"10px 2rem"}}>
       {Array(10).fill(1).map((_,i)=> <Course key={i}
          course={{
            members_url: "/api/courses/1/members",
            name: "Computationele biologie",
            description: "Een cursus over computationele biologie",
            id: 1,
            teachers: [],
          }}
          />)}
        
      </Space>
      </div>
      <br/><br/>
      <div style={{ position: "relative", padding:"0 2rem" }}>
      <Typography.Title level={3}>
        {t("home.yourProjects")}
      </Typography.Title>

       <ProjectCard/>
    

      <CreateCourseModal open={open} setOpen={setOpen} />
          </div>
    </div>
  )
}

export default Home
