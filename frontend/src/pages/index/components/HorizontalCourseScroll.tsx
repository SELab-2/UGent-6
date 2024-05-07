import { Button, Select, Space, Typography } from "antd"
import useUser from "../../../hooks/useUser"
import CourseCard from "./CourseCard"
import { FC, useEffect, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { PlusOutlined, RightOutlined } from "@ant-design/icons"
import { ProjectsType } from "../Home"
import TeacherView from "../../../hooks/TeacherView"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"

const { Option } = Select;

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
  const [archivedCourses, setArchivedCourses] = useState<boolean>(false)
  const [selectedYear, setSelectedYear] = useState<number>(null)

  const { t } = useTranslation()
  const navigate = useNavigate()

  useEffect(() => {
    if (courses === null || projects === null) return () => {}
    let courseProjects: CourseProjectsType = {}
    let adminCourseProjects: CourseProjectsType = {}
    let ignore = false
    let hasArchivedCourses = false

    courses.forEach((course) => {
      if(course.archivedAt) return hasArchivedCourses = true; // We don't want to show archived courses

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

    if (courses && courses.length > 0) {
      const allYears = courses.map(course => course.year);
      setSelectedYear(Math.max(...allYears))
    }

    setCourseProjects(courseProjects)
    setAdminCourseProjects(adminCourseProjects)
    setArchivedCourses(hasArchivedCourses)

    return () => (ignore = true)
  }, [courses, projects])

  // Filter courses based on selected year
  const filteredCourseProjects = courseProjects ? Object.values(courseProjects).filter(course => course.course.year === selectedYear) : [];
  const filteredAdminCourseProjects = adminCourseProjects ? Object.values(adminCourseProjects).filter(course => course.course.year === selectedYear) : [];

  // Generate options for the year dropdown
  const yearOptions = Array.from(new Set([...(courses || []).map(course => course.year)]));

  return (
      <>
        {/* Dropdown for selecting year */}
        {yearOptions.length > 1 && (
        <div style={{paddingLeft: '1rem', marginBottom: 0, paddingBottom:0}}>
          <Select
              variant='borderless'
              value={selectedYear}
              onChange={(value: number) => setSelectedYear(value)}
              style={{width: 120}}
          >
            {yearOptions.map(year => (
                <Option key={year} value={year}>{`${year} - ${year + 1}`}</Option>
            ))}
          </Select>
        </div>
        )}

        {filteredCourseProjects.length > 0 && (
            <>
              <Typography.Title
                  level={3}
                  style={{
                    paddingLeft: "2rem",
                    marginTop: 0,
                    paddingTop: 0
                  }}
              >
                {t("home.yourCourses")}
              </Typography.Title>
              <Space
                  className="small-scroll-bar"
                  style={{
                    display: 'flex',
                    maxWidth: "100%",
                    overflowX: "auto",
                    whiteSpace: "nowrap",
                    padding: "10px 2rem"
                  }}
              >
                {filteredCourseProjects.map((c) => (
                    <div key={c.course.courseId} style={{height: '100%'}}>
                      <CourseCard courseProjects={c}/>
                    </div>
                ))}
              </Space>
            </>
        )}

        {filteredAdminCourseProjects && filteredAdminCourseProjects.length > 0 && (
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
                          style={{ marginLeft: "1rem",
                                   marginTop: 0,
                                   paddingTop: 0
                          }}
                          icon={<PlusOutlined />}
                      />
                    </TeacherView>
                )}

                {(archivedCourses || filteredAdminCourseProjects.length > 2 )&& <Button
                    type="link"
                    style={{ float: "right" }}
                    onClick={() => navigate(AppRoutes.COURSES+"?role=admin")}
                >
                  {t("home.moreCourses")} <RightOutlined />
                </Button>}
              </Typography.Title>
              <Space
                  className="small-scroll-bar"
                  style={{ display: 'flex', maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap", padding: "10px 2rem", alignItems: "stretch" }}
              >
                {filteredAdminCourseProjects.map((c) => (
                    <CourseCard
                        key={c.course.courseId}
                        courseProjects={c}
                        adminView
                    />
                ))}
              </Space>
            </>
        )}

        {filteredCourseProjects.length === 0 && filteredAdminCourseProjects.length === 0 && (
            <Typography.Text
                style={{
                  paddingLeft: "2rem",
                }}
            >
              {t("home.noCourses")}
            </Typography.Text>
        )}
      </>
  )
}

export default HorizontalCourseScroll
