import { FC, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { useParams } from "react-router-dom"
import { ApiRoutes, GET_Responses } from "../../@types/requests"
import { Card, Spin, Tabs, Typography, theme } from "antd"
import { TabsProps } from "antd/lib"
import ProjectCard from "../index/components/ProjectCard"
import GroupsCard from "./components/GroupsCard"

export type CourseType = GET_Responses[ApiRoutes.COURSE]

const Course: FC = () => {
  const { t } = useTranslation()
  const params = useParams<{ id: string }>()
  const [course, setCourse] = useState<CourseType | null>(null)

  const items: TabsProps["items"] = useMemo(() =>[
    {
      key: "1",
      label: t("course.projects"),
      children: <ProjectCard />,
    },
    {
      key: "2",
      label: t("course.groups"),
      children: <GroupsCard courseId={params.id!} />,
    },
    {
      key: "4",
      label: t("course.grades"),
      children: "GRADES",
    },
    {
      key: "3",
      label: t("course.info"),
      children: "Content of Tab Pane 3",
    },
  ],[t])

  useEffect(() => {
    // TODO: fetch course data

    setTimeout(() => {
      setCourse({
        members_url: "/api/courses/1/members",
        name: "Computationele biologie",
        description: "Een cursus over computationele biologie",
        id: 1,
        teachers: [],
      })
    }, 250)
  }, [params.id])

  if (course === null) {
    return (
      <div style={{ width: "100%", height: "100%", display: "flex", justifyContent: "center", alignItems: "center" }}>
        <Spin
          tip="Loading..."
          size="large"
        />
      </div>
    )
  }

  return (
    <div style={{ marginTop: "3rem" }}>
      <div style={{ padding: "0 2rem" }}>
        <Typography.Title level={1}>{course.name}</Typography.Title>
        <Tabs
          defaultActiveKey="1"
          items={items}
        />
      </div>
    </div>
  )
}

export default Course
