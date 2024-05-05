import { LogoutOutlined, MoreOutlined, PlusOutlined } from "@ant-design/icons"
import { Button, Dropdown } from "antd"
import { MenuProps } from "antd/lib";
import { FC, useContext } from "react";
import { useTranslation } from "react-i18next";
import { CourseContext } from "../../../../router/CourseRoutes";
import { useNavigate } from "react-router-dom";
import { AppRoutes } from "../../../../@types/routes";
import { UserContext } from "../../../../providers/UserProvider";
import { leaveCourse } from "./CourseMembershipService";




const CourseAdminBtn:FC<{courseId:string}> = ({courseId}) => {
  const {t} = useTranslation()
  const { member } = useContext(CourseContext)
  const navigate = useNavigate()
  const  userContext  = useContext(UserContext)


  const leaveCourseHandler = async () => {
    const result = await leaveCourse(courseId, t);
    await userContext.updateCourses()
    navigate(AppRoutes.HOME);
  }


  const items: MenuProps['items'] = [
    {
      key: '1',
      label: t("project.change.create"),
      icon: <PlusOutlined/>,
      onClick: () => navigate(AppRoutes.PROJECT_CREATE.replace(":courseId", courseId))
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
