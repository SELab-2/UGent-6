import { FC, useMemo } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../@types/requests"
import { Space, Tabs, Tag, Typography } from "antd"
import { TabsProps } from "antd/lib"
import ProjectCard from "../index/components/ProjectCard"
import GroupsCard from "./components/groupTab/GroupsCard"
import useCourse from "../../hooks/useCourse"
import useIsCourseAdmin from "../../hooks/useIsCourseAdmin"
import MembersCard from "./components/membersTab/MemberCard"
import SettingsCard from "./components/settingsTab/SettingsCard"
import GradesCard from "./components/gradesTab/GradesCard"

export type CourseType = GET_Responses[ApiRoutes.COURSE]

const Course: FC = () => {
  const { t } = useTranslation()
  const course = useCourse()
  const isCourseAdmin = useIsCourseAdmin()

  const items: TabsProps["items"] = useMemo(() => {
    let tabs: TabsProps["items"] = [
      // {
      //   key: "1",
      //   label: t("course.info"),
      //   children: "Content of Tab Pane 3",
      // },
      {
        key: "1",
        label: t("course.projects"),
        children: <ProjectCard />,
      },
      {
        key: "2",
        label: t("course.groups"),
        children: <GroupsCard courseId={course.courseId!} />,
      },
     
      
    ]

    if (isCourseAdmin) {

      tabs = tabs.concat([
        {
          key: "5",
          label: t("course.members"),
          children: <MembersCard />
        },
        {
          key: "6",
          label: t("course.settings"),
          children: <SettingsCard />,
        },
      ])
    } else {
      tabs = tabs.concat([
        {
          key: "4",
          label: t("course.grades"),
          children: <GradesCard />
        },
      ])
    }

    return tabs
  }, [t, isCourseAdmin])

  return (
    <div style={{ marginTop: "3rem" }}>
      <div style={{ padding: "0 2rem" }}>
        <Typography.Title style={{marginBottom:"0.5rem"}} level={1}>{course.name}</Typography.Title>
        <Space direction="horizontal" size="small" style={{marginBottom:"0.5rem"}}>
          <Tag color="blue">2024-2025</Tag> 
          {
            course.teachers.map((teacher) => (

              <Tag key={teacher.url} color="orange">{teacher.name} {teacher.surname}</Tag>
            ))
          }
        </Space>
        <br/>
        <Tabs
          defaultActiveKey="1"
          items={items}
        />
      </div>
    </div>
  )
}

export default Course
