import { AuthenticatedTemplate, UnauthenticatedTemplate } from "@azure/msal-react"
import UnauthNav from "./UnauthNav"
import AuthNav from "./AuthNav"
import { FC, PropsWithChildren } from "react"
import { Layout as AntLayout, Button, Dropdown, MenuProps, Typography } from "antd"
import Logo from "../../Logo"
import { GlobalOutlined } from "@ant-design/icons"
import useApp from "../../../hooks/useApp"
import { Language } from "../../../@types/types"

const items: MenuProps["items"] = [
  {
    key: Language.EN,
    label: "English",
  },
  {
    key: Language.NL,
    label: "Nederlands",
  },
]

const Layout: FC<PropsWithChildren> = ({ children }) => {
  const app = useApp()

  const languageChange: MenuProps["onClick"] = (props) => {
    app.setLanguage(props.key as Language)
  }

  return (
    <div style={{ position: "fixed", width: "100vw" }}>
      <AntLayout.Header style={{ display: "flex", alignItems: "center", gap: "2rem" }}>
        <Logo style={{ margin: 0, padding: 0, width: "100%" }} />
        <UnauthenticatedTemplate>
          <UnauthNav />
        </UnauthenticatedTemplate>
        <AuthenticatedTemplate>
          <AuthNav />
        </AuthenticatedTemplate>

        <Dropdown menu={{ items, onClick: languageChange }}>
        <Typography.Text style={{cursor:"pointer",width:"5rem"}}><GlobalOutlined /> {app.language}</Typography.Text>
        
        </Dropdown>
      </AntLayout.Header>
      <AntLayout style={{ height: "calc(100vh - 48px)", overflow: "auto" }}>
        <AntLayout.Content>{children}</AntLayout.Content>

        <AntLayout.Footer></AntLayout.Footer>
      </AntLayout>
    </div>
  )
}

export default Layout
