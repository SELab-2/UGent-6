import { AuthenticatedTemplate, UnauthenticatedTemplate } from "@azure/msal-react"
import UnauthNav from "./UnauthNav"
import AuthNav from "./AuthNav"
import { FC, PropsWithChildren } from "react"
import { Layout as AntLayout } from "antd"
import Logo from "../../Logo"

const Layout: FC<PropsWithChildren> = ({ children }) => {
  return (
    <div
      style={{position:"fixed",width:"100vw" }}
    >
      <AntLayout.Header style={{ display: "flex", alignItems: "center"}}>
        <Logo style={{ margin: 0, padding: 0, width: "100%" }} />
        <UnauthenticatedTemplate>
          <UnauthNav />
        </UnauthenticatedTemplate>
        <AuthenticatedTemplate>
          <AuthNav />
        </AuthenticatedTemplate>
      </AntLayout.Header>
      <AntLayout style={{ height: "calc(100vh - 48px)",overflow:"auto" }}>
        <AntLayout.Content>{children}</AntLayout.Content>

        <AntLayout.Footer></AntLayout.Footer>
      </AntLayout>
    </div>
  )
}

export default Layout
