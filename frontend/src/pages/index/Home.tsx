import { Button, Space, Typography } from "antd"
import { useTranslation } from "react-i18next"
import CourseCard from "./components/CourseCard"
import { PlusOutlined } from "@ant-design/icons"
import CreateCourseModal from "./components/CreateCourseModal"
import { useState } from "react"
import ProjectCard from "./components/ProjectCard"
import useUser from "../../hooks/useUser"

const Home = () => {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const {courses} = useUser()
  
  return (
    <div >
      <div>
        <Typography.Title
          level={3}
          style={{
            paddingLeft: "2rem",
          }}
        >
          {t("home.yourCourses")}
          {/* <TeacherView> */}{" "}
          <Button
            onClick={() => setOpen(true)}
            type="text"
            icon={<PlusOutlined />}
          />
          {/* </TeacherView> */}
        </Typography.Title>

        <Space
          className="small-scroll-bar"
          style={{ maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap", padding: "10px 2rem" }}
        >
          {courses
            .map((c) => (
              <CourseCard
                key={c.courseId}
                course={c}
              />
            ))}
        </Space>
      </div>
      <br />
      <br />
      <div style={{ position: "relative", padding: "0 2rem" }}>
        <Typography.Title level={3}>{t("home.yourProjects")}</Typography.Title>

        <ProjectCard />

        <CreateCourseModal
          open={open}
          setOpen={setOpen}
        />
      </div>
    </div>
  )
}

export default Home
