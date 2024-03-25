import { useIsAuthenticated, useMsal } from "@azure/msal-react"
import Home from "./Home"
import LandingPage from "./landing/LandingPage"
import { InteractionStatus } from "@azure/msal-browser"
import { Spin } from "antd"

const HomeAuthCheck = () => {
  const isAuthenticated = useIsAuthenticated()
  const { inProgress } = useMsal()

  if (inProgress !== InteractionStatus.None)
    return (
      <div
        style={{
          width: "100%",
          height: "100%",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <Spin size="large" />
      </div>
    )
  if (isAuthenticated) {
    return <Home />
  }
  return <LandingPage />
}

export default HomeAuthCheck
