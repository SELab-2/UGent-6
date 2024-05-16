import { CopyOutlined, LogoutOutlined, MoreOutlined, PlusOutlined, TeamOutlined } from "@ant-design/icons"
import { Button, Dropdown } from "antd"
import { MenuProps } from "antd/lib";
import { FC, useContext } from "react";
import { useTranslation } from "react-i18next";
import { CourseContext } from "../../../../router/CourseRoutes";
import { useNavigate } from "react-router-dom";
import { AppRoutes } from "../../../../@types/routes";
import { UserContext } from "../../../../providers/UserProvider";
import { leaveCourse } from "./CourseMembershipService";
import useApi from "../../../../hooks/useApi";
import { ApiRoutes } from "../../../../@types/requests.d";
import openInviteModal from "./InviteModalContent";
import useCourse from "../../../../hooks/useCourse";
import useAppApi from "../../../../hooks/useAppApi";




const CourseAdminBtn:FC<{courseId:string}> = ({courseId}) => {
  const {t} = useTranslation()
  const { member } = useContext(CourseContext)
  const navigate = useNavigate()
  const course = useCourse()
  const {setCourse} = useContext(CourseContext)
  const  userContext  = useContext(UserContext)
  const API = useApi()
  const {modal} = useAppApi()


  const leaveCourseHandler = async () => {
    await leaveCourse(courseId, t);
    await userContext.updateCourses()
    navigate(AppRoutes.HOME);
  }

  const makeCopy = async () => {
    const course = await API.POST(ApiRoutes.COURSE_COPY, {body: undefined, pathValues: {courseId: courseId}}, {
     mode: "message",
     successMessage: t("course.copySuccess"), 
    })
    if(!course.success) return
    await userContext.updateCourses()

    navigate(AppRoutes.COURSE.replace(":courseId", course.response.data.courseId+""))
  }

  const items: MenuProps['items'] = [
    {
      key: '1',
      label: t("project.change.create"),
      icon: <PlusOutlined/>,
      onClick: () => navigate(AppRoutes.PROJECT_CREATE.replace(":courseId", courseId))
    },
    {
      key: '3',
      label: t("course.copy"),
      icon: <CopyOutlined/>,
      onClick: makeCopy
    },
    {
      key: '4',
      label: t("course.invitePeople"),
      icon: <TeamOutlined/>, 
      onClick: () => openInviteModal({modal,course, title: t("course.invitePeopleToCourse"), onChange: setCourse})
    },
    {
      key: '2',
      label: t("course.leave"),
      icon: <LogoutOutlined/>,
      danger:true,
      disabled: member.relation === "creator",
      onClick: leaveCourseHandler
    }
  ];

  
  return (
    <>
      <Dropdown menu={{ items }}>
        <Button
          icon={<MoreOutlined />}
          type="text"
        />
      </Dropdown>
    </>
  )
}

export default CourseAdminBtn
