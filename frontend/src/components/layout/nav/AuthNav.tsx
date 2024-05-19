
import { Dropdown, MenuProps, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { UserOutlined, BgColorsOutlined, DownOutlined, LogoutOutlined } from "@ant-design/icons"
import { useNavigate } from "react-router-dom"
import { Themes } from "../../../@types/appTypes"
import { AppRoutes } from "../../../@types/routes"
import useApp from "../../../hooks/useApp"
import useAuth from "../../../hooks/useAuth"


const AuthNav = () => {
  const { t } = useTranslation()
  const app = useApp()
  const auth = useAuth()
  const navigate = useNavigate()

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
        }
      ]
    },
    {
      key: "logout",
      label: t("nav.logout"),
      icon: <LogoutOutlined />,
    },
  ]

  const handleDropdownClick: MenuProps["onClick"] = (menu) => {
    switch (menu.key) {
      case "profile":
        navigate(AppRoutes.PROFILE)
        break
      case "logout":
        auth.logout()
        window.location.replace("http://localhost:3000/web/auth/signout")
        break
      case Themes.DARK:
      case Themes.LIGHT:
        app.setTheme(menu.key as Themes)
        break
    }
  }
  
  return (<>
    <div
      style={{
        width: "100%",
        display: "flex",
        justifyContent: "end",
      }}
    >
      <Dropdown menu={{ items, onClick: handleDropdownClick }}>
        <Typography.Text style={{cursor:"pointer"}}>{auth!.account?.name} <DownOutlined /></Typography.Text>
      </Dropdown>
    </div>
    
    </>
  )
}

export default AuthNav
