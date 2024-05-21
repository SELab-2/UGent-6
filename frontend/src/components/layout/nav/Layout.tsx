
import AuthNav from "./AuthNav"
import { FC, PropsWithChildren } from "react"
import { Layout as AntLayout, Flex } from "antd"
import Logo from "../../Logo"

import Sidebar from "../sidebar/Sidebar"
import LanguageDropdown from "../../LanguageDropdown"
import useAuth from "../../../hooks/useAuth";

const Layout: FC<PropsWithChildren<{}>> = ({ children }) => {
  const auth = useAuth()



  if(!auth.isAuthenticated) return <>{children}</>

  return (
    <div style={{ position: "fixed", width: "100vw" }}>
        <AntLayout.Header style={{ display: "flex", alignItems: "center", gap: "2rem" }}>
          <Sidebar />
          <Logo className="nav-logo" style={{ margin: 0, padding: 0, width: "100%" }} />
          {/* <UnauthenticatedTemplate>
          <UnauthNav />
        </UnauthenticatedTemplate> */}
          <AuthNav/>
          <LanguageDropdown/>
        </AntLayout.Header>
      <AntLayout style={{ height: "calc(100vh - 48px)", overflow: "auto" }}>
        <AntLayout.Content>
          <Flex
            style={{
              width: "100%",
              height: "100%",
              marginBottom: "3rem",
            }}
            justify="center"
          >
            <div style={{ maxWidth: "1200px", width: "100%", height: "100%",margin: "0 1rem" }}>{children}</div>
          </Flex>
          {/* <AntLayout.Footer style={{ height: "2rem", width: "100%", bottom: 0 }}></AntLayout.Footer> */}
        </AntLayout.Content>
      </AntLayout>
    </div>
  )
}

export default Layout
