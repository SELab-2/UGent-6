import { AuthenticatedTemplate, UnauthenticatedTemplate } from "@azure/msal-react"
import UnauthNav from "./UnauthNav"
import AuthNav from "./AuthNav"
import { FC, PropsWithChildren } from "react"

const Nav: FC<PropsWithChildren> = ({ children }) => {
  return (
    <div>
      <div style={{ width: "100%", background: "rgba(0,0,0,0.1)", height: "3rem", position: "fixed", top: 0 }}>
        <UnauthenticatedTemplate>
          <UnauthNav />
        </UnauthenticatedTemplate>

        <AuthenticatedTemplate>
          <AuthNav />
        </AuthenticatedTemplate>
      </div>

      <div style={{position:"relative", top:"3rem"}}>
         {children}

      </div>
    </div>
  )
}

export default Nav
