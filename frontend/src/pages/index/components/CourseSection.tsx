import { Select, Typography } from "antd"
import useUser from "../../../hooks/useUser"
import { FC, useEffect, useMemo, useState } from "react"
import { ApiRoutes, GET_Responses } from "../../../@types/requests.d"
import { useTranslation } from "react-i18next"
import { ProjectsType } from "../Home"
import HorizontalCourseScroll from "./HorizontalCourseScroll"

const { Option } = Select

export type CourseProjectsType = {
  [courseId: string]: {
    projects: GET_Responses[ApiRoutes.COURSE_PROJECTS]
    course: GET_Responses[ApiRoutes.COURSES][number]
  }
}

export type CourseProjectList = CourseProjectsType[string][] | null

const CourseSection: FC<{ projects: ProjectsType | null; onOpenNew: () => void }> = ({ projects, onOpenNew }) => {
  const { courses } = useUser()
  const [courseProjects, setCourseProjects] = useState<CourseProjectsType | null>(null)
  const [adminCourseProjects, setAdminCourseProjects] = useState<CourseProjectsType | null>(null)
  const [archivedCourses, setArchivedCourses] = useState<boolean>(false)
  const [selectedYear, setSelectedYear] = useState<number | null>(null)

  const { t } = useTranslation()

  useEffect(() => {
    if (courses === null || projects === null) return () => {}
    let courseProjects: CourseProjectsType = {}
    let adminCourseProjects: CourseProjectsType = {}
    let ignore = false
    let hasArchivedCourses = false

    courses.forEach((course) => {
      if (course.archivedAt) return (hasArchivedCourses = true) // We don't want to show archived courses

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
      const allYears = courses.map((course) => course.year)
      setSelectedYear(Math.max(...allYears))
    }

    setCourseProjects(courseProjects)
    setAdminCourseProjects(adminCourseProjects)
    setArchivedCourses(hasArchivedCourses)

    return () => (ignore = true)
  }, [courses, projects])


  const [filteredCourseProjects, filteredAdminCourseProjects, courseProjectsList, adminCourseProjectsList, yearOptions]: [CourseProjectList, CourseProjectList, CourseProjectList, CourseProjectList, number[] | null] = useMemo(() => {
    // Filter courses based on selected year
    if (courseProjects === null || adminCourseProjects === null) return [null, null, [], [], null]
    const courseProjectsList: CourseProjectList = Object.values(courseProjects)
    const adminCourseProjectsList: CourseProjectList = Object.values(adminCourseProjects)
    const filteredCourseProjects = courseProjectsList.filter((course) => course.course.year === selectedYear)
    const filteredAdminCourseProjects = adminCourseProjectsList.filter((course) => course.course.year === selectedYear)

    // Generate options for the year dropdown
    const yearOptions = Array.from(new Set([...(courses || []).map((course) => course.year)]))

    return [filteredCourseProjects, filteredAdminCourseProjects, courseProjectsList, adminCourseProjectsList, yearOptions]
  }, [courseProjects, adminCourseProjects, selectedYear])

  const YearDropdown = () => (
    <>
      {yearOptions && yearOptions.length > 1 && (
        <div style={{ paddingLeft: "1rem", marginBottom: 0, paddingBottom: 0 }}>
          <Select
            variant="borderless"
            value={selectedYear}
            onChange={(value: number) => setSelectedYear(value)}
            style={{ width: 120 }}
          >
            {yearOptions.map((year) => (
              <Option
                key={year}
                value={year}
              >{`${year} - ${year + 1}`}</Option>
            ))}
          </Select>
        </div>
      )}
    </>
  )

  const showYourCourses = !!filteredCourseProjects?.length || !filteredAdminCourseProjects?.length
  return (
    <>
      {/* Dropdown for selecting year */}

     {!!showYourCourses && <HorizontalCourseScroll
        title={t("home.yourCourses")}
        projects={filteredCourseProjects}
        onOpenNew={onOpenNew}
        showMore={archivedCourses || courseProjectsList.length > 2}
        showPlus={!filteredAdminCourseProjects?.length}
        extra={YearDropdown}
        allOptions={showYourCourses}
        type="enrolled"
      />}
  

      { !!filteredAdminCourseProjects?.length && <HorizontalCourseScroll
        title={t("home.myCourses")}
        projects={filteredAdminCourseProjects}
        onOpenNew={onOpenNew}
        showMore={archivedCourses || adminCourseProjectsList.length > 2}
        extra={YearDropdown}
        showPlus={!!filteredAdminCourseProjects?.length}
        allOptions={!!filteredAdminCourseProjects?.length && !filteredCourseProjects?.length}
        type="admin"
      />}

     

      {filteredCourseProjects !== null && courseProjectsList.length === 0 && adminCourseProjectsList.length === 0 && (
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

export default CourseSection
