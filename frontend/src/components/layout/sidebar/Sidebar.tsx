import { BookOutlined, MenuOutlined, UserOutlined } from "@ant-design/icons"
import { Button, Drawer, Menu, MenuProps } from "antd"
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
    navigate(AppRoutes.COURSE.replace(":courseId", menu.key as string))
    setOpen(false)
  }

  const menuItems: MenuProps["items"] = useMemo(
    () => [
      {
        key: "courses",
        label: t("home.allCourses"),
        type: "sub1",
        children: (courses ?? []).map((c) => ({
          key: c.courseId,
          label: c.name,
        })),
      },
    ],
    [courses, t]
  )

  const openProfile = () => {
    navigate(AppRoutes.PROFILE)
    setOpen(false)
  }

  return (
    <>
      <Button
        icon={<MenuOutlined />}
        type="text"
        onClick={() => setOpen(true)}
        size="large"
        style={{ width: "100px", background: "100px" }}
      />
      <Drawer
        getContainer={false}
        placement="left"
        title="Pigeonhole"
        closable={false}
        onClose={() => setOpen(false)}
        open={open}
        styles={{
          body: {
            padding: 0,
          },
        }}
        footer={
          <>
            <Button
              type="text"
              style={{ width: "100%" }}
              icon={<UserOutlined />}
              size="large"
              onClick={openProfile}
            >
              Profile
            </Button>

            <Button
              type="text"
              style={{ width: "100%" }}
              size="large"
              icon={<BookOutlined />}
              onClick={() => window.open("https://github.com/SELab-2/UGent-6/wiki", "_blank")}
            >
              {t("home.docs")}
            </Button>
          </>
        }
      >
        <Menu
          style={{
            background: "transparent",
          }}
          theme="light"
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
