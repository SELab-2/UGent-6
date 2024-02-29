import { useAccount } from "@azure/msal-react"
import { Dropdown, MenuProps, Typography } from "antd"
import { useTranslation } from "react-i18next"
import { BgColorsOutlined, DownOutlined, LogoutOutlined } from "@ant-design/icons"
import { msalInstance } from "../../../index"
import { Themes } from "../../../@types/types"
import useApp from "../../../hooks/useApp"

const AuthNav = () => {
  const { t } = useTranslation()
  const app = useApp()
  const auth = useAccount()

  const items: MenuProps["items"] = [
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
        {
          key: Themes.DODONA,
          label: "Dodona",
          
        },
      ]
    },
    {
      key: "logout",
      label: t("nav.logout"),
      icon: <LogoutOutlined />,
    },
  ]

  const handleDropdownClick: MenuProps["onClick"] = (menu) => {
    console.log(menu.keyPath,menu.key);
    switch (menu.key) {
      case "logout":
        console.log(auth);
        msalInstance.logoutPopup({
          account: auth,
        })
        break
      case Themes.DARK:
      case Themes.DODONA:
      case Themes.LIGHT:
        app.setTheme(menu.key as Themes)
        break
    }
  }
  
  return (
    <div
      style={{
        width: "100%",
        display: "flex",
        justifyContent: "end",
      }}
    >
      <Dropdown menu={{ items, onClick: handleDropdownClick }}>
        <Typography.Text style={{cursor:"pointer"}}>{auth!.name} <DownOutlined /></Typography.Text>
      </Dropdown>
    </div>
  )
}

export default AuthNav
