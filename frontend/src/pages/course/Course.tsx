import { FC, useMemo } from "react"
import { useTranslation } from "react-i18next"
import { ApiRoutes, GET_Responses } from "../../@types/requests.d"
import { Tabs, Typography } from "antd"
import { TabsProps } from "antd/lib"
import ProjectCard from "../index/components/ProjectCard"
import GroupsCard from "./components/groupTab/GroupsCard"
import useCourse from "../../hooks/useCourse"
import useIsCourseAdmin from "../../hooks/useIsCourseAdmin"
import MembersCard from "./components/membersTab/MemberCard"
import SettingsCard from "./components/settingsTab/SettingsCard"
import GradesCard from "./components/gradesTab/GradesCard"
import { useLocation, useNavigate } from "react-router-dom"
import InformationTab from "./components/informationTab/InformationTab"
import { InfoCircleOutlined, ScheduleOutlined, SettingOutlined, TeamOutlined, UnorderedListOutlined, UserOutlined } from "@ant-design/icons"
import ExtraTabBtn from "./components/tabExtraBtn/ExtraTabBtn"
import CourseTagLine from "./components/tabExtraBtn/CourseTagLine"

export type CourseType = GET_Responses[ApiRoutes.COURSE]

const Course: FC = () => {
  const { t } = useTranslation()
  const course = useCourse()
  const isCourseAdmin = useIsCourseAdmin()
  const navigate = useNavigate()
  const location = useLocation()

  const items: TabsProps["items"] = useMemo(() => {
    let tabs: TabsProps["items"] = [
      {
        key: "info",
        label: t("course.info"),
        icon: <InfoCircleOutlined />,
        children: <InformationTab />,
      },
      {
        key: "projects",
        label: t("course.projects"),
        icon: <UnorderedListOutlined />,
        children: <ProjectCard courseId={course.courseId} />,
      },
      {
        key: "groups",
        label: t("course.groups"),
        icon: <TeamOutlined />,
        children: <GroupsCard courseId={course.courseId!} />,
      },
    ]
    if (isCourseAdmin) {
      tabs = tabs.concat([
        {
          key: "members",
          label: t("course.members"),
          icon: <UserOutlined />,
          children: <MembersCard />,
        },
        {
          key: "settings",
          label: t("course.settings"),
          icon: <SettingOutlined />,
          children: <SettingsCard />,
        },
      ])
    } else {
      tabs = tabs.concat([
        {
          key: "grades",
          label: t("course.grades"),
          icon: <ScheduleOutlined />,
          children: <GradesCard />,
        },
      ])
    }

    return tabs
  }, [t, isCourseAdmin, course])

  return (
    <div style={{ margin: "3rem 0" }}>
      <div style={{ padding: "0 2rem" }}>
        <Typography.Title
          style={{ marginBottom: "0.5rem" }}
          level={1}
        >
          {course.name}
        </Typography.Title>
       <CourseTagLine course={course}/>
        <br />
        <Tabs
          onChange={(k) => navigate(`#${k}`)}
          defaultActiveKey={location.hash.slice(1) || "1"}
          items={items}
          tabBarExtraContent={<ExtraTabBtn/>}
        />
      </div>
      <br />
      <br />
    </div>
  )
}

export default Course
