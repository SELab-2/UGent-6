import { Button, Card, Space, Typography } from "antd"
import useUser from "../../../hooks/useUser"
import CourseCard from "./CourseCard"
import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { CaretRightFilled, CaretRightOutlined, PlusOutlined, RightOutlined } from "@ant-design/icons"
import { ProjectsType } from "../Home"
import TeacherView from "../../../hooks/TeacherView"
import { useNavigate } from "react-router-dom"

export type CourseProjectsType = {
  [courseId: string]: {
    projects: GET_Responses[ApiRoutes.COURSE_PROJECTS]
    course: GET_Responses[ApiRoutes.COURSES][number]
  }
}

const HorizontalCourseScroll: FC<{ projects: ProjectsType | null; onOpenNew: () => void }> = ({ projects, onOpenNew }) => {
  const { courses } = useUser()
  const [courseProjects, setCourseProjects] = useState<CourseProjectsType | null>(null)
  const [adminCourseProjects, setAdminCourseProjects] = useState<CourseProjectsType | null>(null)
  const { t } = useTranslation()
  const navigate = useNavigate()

  useEffect(() => {
    if (courses === null || projects === null) return () => {}
    let courseProjects: CourseProjectsType = {}
    let adminCourseProjects: CourseProjectsType = {}
    let ignore = false

    courses.forEach((course) => {
      if(course.archivedAt) return // We don't want to show archived courses

      if (course.relation === "enrolled") {
        courseProjects[course.courseId] = { course: course, projects: [] }
      } else {
        adminCourseProjects![course.courseId] = { course: course, projects: [] }
      }
    })

    if (ignore) return
    projects.forEach((project) => {
      if (project.course.courseId in courseProjects) {
        courseProjects[project.course.courseId].projects.push(project)
      } else if (project.course.courseId in adminCourseProjects) {
        adminCourseProjects[project.course.courseId].projects.push(project)
      } else {
        // This shouldn't happen unless there's a backend bug
        console.error("User is in a project while not being in the course! ", project, courses)
      }
    })

    setCourseProjects(courseProjects)
    setAdminCourseProjects(adminCourseProjects)

    return () => (ignore = true)
  }, [courses, projects])

  const courseProjectsArray = courseProjects ? Object.values(courseProjects) : []
  const adminCourseProjectsArray = adminCourseProjects ? Object.values(adminCourseProjects) : []

  return (
    <>
      {courseProjects && adminCourseProjectsArray.length && !courseProjectsArray.length ? null : (
        <Typography.Title
          level={3}
          style={{
            paddingLeft: "2rem",
          }}
        >
          {t("home.yourCourses")}
         {adminCourseProjectsArray.length === 0 && <TeacherView>
            <Button
              onClick={onOpenNew}
              type="text"
              style={{ marginLeft: "1rem" }}
              icon={<PlusOutlined />}
            />
          </TeacherView>}

          {courseProjectsArray.length > 2 && <Button
              type="link"
              style={{ float: "right" }}
              onClick={() => navigate("/courses?role=enrolled")}
            >
              {t("home.moreCourses")} <RightOutlined />
            </Button>}
        </Typography.Title>
      )}

      {courseProjects && !adminCourseProjectsArray.length && !courseProjectsArray.length && (
        <Typography.Text
          style={{
            paddingLeft: "2rem",
          }}
        >
          {t("home.noCourses")}
        </Typography.Text>
      )}
      <Space
        className="small-scroll-bar"
        style={{ maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap", padding: "10px 2rem" }}
      >
        {courseProjects !== null
          ? courseProjectsArray.map((c) => (
              <CourseCard
                key={c.course.courseId}
                courseProjects={c}
              />
            ))
          : Array(3)
              .fill(0)
              .map((_, i) => (
                <Card
                  key={i}
                  loading
                  style={{ width: 300, height: 235 }}
                />
              ))}
      </Space>

      {adminCourseProjects && !!adminCourseProjectsArray.length && (
        <>
          <Typography.Title
            level={3}
            style={{
              paddingLeft: "2rem",
            }}
          >
            {t("home.myCourses")}

            {courseProjects && (
              <TeacherView>
                <Button
                  onClick={onOpenNew}
                  type="text"
                  style={{ marginLeft: "1rem" }}
                  icon={<PlusOutlined />}
                />
              </TeacherView>
            )}

            {adminCourseProjectsArray.length > 2 && <Button
              type="link"
              style={{ float: "right" }}
              onClick={() => navigate("/courses?role=admin")}
            >
              {t("home.moreCourses")} <RightOutlined />
            </Button>}
          </Typography.Title>
          <Space
            className="small-scroll-bar"
            style={{ maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap", padding: "10px 2rem", alignItems: "stretch" }}
          >
            {adminCourseProjectsArray.map((c) => (
              <CourseCard
                key={c.course.courseId}
                courseProjects={c}
                adminView
              />
            ))}
          </Space>
        </>
      )}
    </>
  )
}

export default HorizontalCourseScroll
