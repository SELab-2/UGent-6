import { MenuOutlined } from "@ant-design/icons"
import { Button, Drawer, List, Menu, MenuProps, Typography } from "antd"
import { FC, useMemo, useState } from "react"
import useUser from "../../../hooks/useUser"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { AppRoutes } from "../../../@types/routes"

const Sidebar: FC = () => {
  const [open, setOpen] = useState(false)
  const { courses } = useUser()
  const { t } = useTranslation()
  const navigate = useNavigate()

  const onClick: MenuProps["onClick"] = (menu) => {
    console.log(menu);

    navigate(AppRoutes.COURSE.replace(":id",menu.key as string))
    setOpen(false)
  }

  const menuItems: MenuProps["items"] = useMemo(
    () => [
      {
        key: "courses",
        label: t("home.yourCourses"),
        type: "sub1",
        children: courses.map((c) => ({
          key: c.courseId,
          label: c.name,
          
        })),
      },
    ],
    [courses,t]
  )

  return (
    <>
      <Button
        icon={<MenuOutlined />}
        type="text"
        onClick={() => setOpen(true)}
        size="large"
        style={{width:"100px",background:"100px"}}
      />
      <Drawer
        getContainer={false}
        placement="left"
        title="Pigeonhole"
        closable={false}
        onClose={() => setOpen(false)}
        open={open}
        styles={{
          body:{
            padding: 0,
          }
        }}
      >
        <Menu
        style={{
          background:"transparent",
        }}
          onClick={onClick}
          defaultOpenKeys={["courses"]}
          selectedKeys={[]}
          mode="inline"
          items={menuItems}
        />
      </Drawer>
    </>
  )
}

export default Sidebar
