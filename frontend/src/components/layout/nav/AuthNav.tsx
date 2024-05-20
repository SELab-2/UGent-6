import { useAccount } from "@azure/msal-react"
import { Breadcrumb, Dropdown, MenuProps, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { UserOutlined, BgColorsOutlined, DownOutlined, LogoutOutlined, PlusOutlined } from "@ant-design/icons"
import { msalInstance } from "../../../index"
import { useNavigate } from "react-router-dom"
import { Themes } from "../../../@types/appTypes"
import { AppRoutes } from "../../../@types/routes"
import useApp from "../../../hooks/useApp"
import createCourseModal from "../../../pages/index/components/CreateCourseModal"
import useIsTeacher from "../../../hooks/useIsTeacher"

const AuthNav = () => {
  const { t } = useTranslation()
  const app = useApp()
  const auth = useAccount()
  const isTeacher = useIsTeacher()
  const navigate = useNavigate()
  const modal = createCourseModal()

  const items: MenuProps["items"] = [
    {
      key: "profile",
      label: t("nav.profile"),
      icon: <UserOutlined />,
    },
    {
      key: "theme",
      label: t("nav.theme"),
      icon: <BgColorsOutlined />,
      children: [
        {
          key: Themes.LIGHT,
          label: t("nav.light"),
        },
        {
          key: Themes.DARK,
          label: t("nav.dark"),
        },
      ],
    },
  ]
  if (isTeacher) {
    items.push({
      key: "createCourse",
      label: t("home.createCourse"),
      icon: <PlusOutlined />,
    })
  }

  items.push({
    key: "logout",
    label: t("nav.logout"),
    icon: <LogoutOutlined />,
  })

  const handleDropdownClick: MenuProps["onClick"] = (menu) => {
    switch (menu.key) {
      case "profile":
        navigate(AppRoutes.PROFILE)
        break
      case "logout":
        msalInstance.logoutPopup({
          account: auth,
        })
        break
      case Themes.DARK:
      case Themes.LIGHT:
        app.setTheme(menu.key as Themes)
        break
      case "createCourse":
        modal.showModal()
    }
  }

  return (
    <>


      <div
        style={{
          width: "100%",
          display: "flex",
          justifyContent: "end",
        }}
      >
        <Dropdown menu={{ items, onClick: handleDropdownClick }}>
          <Typography.Text style={{ cursor: "pointer" }}>
            {auth!.name} <DownOutlined />
          </Typography.Text>
        </Dropdown>
      </div>
    </>
  )
}

export default AuthNav
