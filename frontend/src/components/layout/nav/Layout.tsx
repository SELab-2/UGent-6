import { AuthenticatedTemplate, UnauthenticatedTemplate } from "@azure/msal-react"
import UnauthNav from "./UnauthNav"
import AuthNav from "./AuthNav"
import { FC, PropsWithChildren } from "react"
import { Layout as AntLayout, Dropdown, Flex, MenuProps, Typography } from "antd"
import Logo from "../../Logo"
import { GlobalOutlined } from "@ant-design/icons"
import useApp from "../../../hooks/useApp"
import { Language } from "../../../@types/appTypes"
import Sidebar from "../sidebar/Sidebar"

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
      <AuthenticatedTemplate>
        <Sidebar/>
      </AuthenticatedTemplate>

        <Logo style={{ margin: 0, padding: 0, width: "100%" }} />
        <UnauthenticatedTemplate>
          <UnauthNav />
        </UnauthenticatedTemplate>
        <AuthenticatedTemplate>
          <AuthNav />
        </AuthenticatedTemplate>

        <Dropdown menu={{ items, onClick: languageChange }}>
          <Typography.Text style={{ cursor: "pointer", width: "5rem" }}>
            <GlobalOutlined /> {app.language}
          </Typography.Text>
        </Dropdown>
      </AntLayout.Header>
      <AntLayout style={{ height: "calc(100vh - 48px)", overflow: "auto" }}>
        <AntLayout.Content>
          <Flex
            style={{
              width: "100%",
              height: "100%",
              marginBottom:"3rem"
            }}
            justify="center"
          >
            <div style={{ maxWidth: "1200px", width: "100%",height:"100%" }}>{children}</div>
          </Flex>
          <AntLayout.Footer style={{ height: "2rem", width: "100%", bottom: 0 }}></AntLayout.Footer>
        </AntLayout.Content>
      </AntLayout>
    </div>
  )
}

export default Layout
