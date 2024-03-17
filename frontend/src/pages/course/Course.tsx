import { FC, useMemo } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../@types/requests"
import { Tabs, Typography } from "antd"
import { TabsProps } from "antd/lib"
import ProjectCard from "../index/components/ProjectCard"
import GroupsCard from "./components/GroupsCard"
import useCourse from "../../hooks/useCourse"

export type CourseType = GET_Responses[ApiRoutes.COURSE]

const Course: FC = () => {
  const { t } = useTranslation()
  const {course} = useCourse()


  const items: TabsProps["items"] = useMemo(() =>[
    {
      key: "1",
      label: t("course.projects"),
      children: <ProjectCard />,
    },
    {
      key: "2",
      label: t("course.groups"),
      children: <GroupsCard courseId={course.id!} />,
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
