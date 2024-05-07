import { Button, Card, Space, Typography } from "antd"
import CourseCard from "./CourseCard"
import { FC } from "react"
import { PlusOutlined, RightOutlined } from "@ant-design/icons"
import TeacherView from "../../../hooks/TeacherView"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"
import { CourseProjectList, CourseProjectsType } from "./CourseSection"
import { useTranslation } from "react-i18next"

const HorizontalCourseScroll: FC<{ title: string; projects: CourseProjectList | null; onOpenNew: () => void; showMore?: boolean; allOptions?: boolean; extra?: () => JSX.Element }> = ({ title, onOpenNew, projects, showMore, allOptions, extra }) => {
  const navigate = useNavigate()
  const { t } = useTranslation()

  return (
    <>
      <div style={{ display: "flex", justifyContent: "space-between", width: "100%" }}>
        <div>
          <Typography.Title
            level={3}
            style={{
              paddingLeft: "2rem",
              display:"flex"
            }}
          >
            {title}{" "}

          {projects && allOptions && (
            <TeacherView>
              <Button
                onClick={onOpenNew}
                type="text"
                style={{ marginLeft: "1rem", marginTop: 0, paddingTop: 0 }}
                icon={<PlusOutlined />}
                />
            </TeacherView>
          )}
          {allOptions && extra && extra()}
          </Typography.Title>
        </div>
        <Typography.Title level={3}>
          <Space style={{ float: "right" }}>
            {showMore && (
              <Button
                type="link"
                style={{ float: "right" }}
                onClick={() => navigate(AppRoutes.COURSES + "?role=admin")}
              >
                {t("home.moreCourses")} <RightOutlined />
              </Button>
            )}
          </Space>
        </Typography.Title>
      </div>

      <Space
        className="small-scroll-bar"
        style={{ display: "flex", maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap", padding: "10px 2rem", alignItems: "stretch" }}
      >
        {projects === null ? (
          <Space
            className="small-scroll-bar"
            style={{ maxWidth: "100%", overflowX: "auto", whiteSpace: "nowrap", padding: "10px 2rem" }}
          >
            {Array(3)
              .fill(0)
              .map((_, i) => (
                <Card
                  key={i}
                  loading
                  style={{ width: 300, height: 235 }}
                />
              ))}
          </Space>
        ) : (
          <>
            {projects.map((c) => (
              <CourseCard
                key={c.course.courseId}
                courseProjects={c}
                adminView
              />
            ))}
          </>
        )}
      </Space>
    </>
  )
}

export default HorizontalCourseScroll
