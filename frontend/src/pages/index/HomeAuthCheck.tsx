import { useIsAuthenticated, useMsal } from "@azure/msal-react"
import Home from "./Home"
import LandingPage from "./landing/LandingPage"

const HomeAuthCheck = () => {
  const isAuthenticated = useIsAuthenticated()
  const { inProgress } = useMsal()

if(inProgress === "startup") return null
  if (isAuthenticated) {
    return <Home />
  }
  return <LandingPage />
}

export default HomeAuthCheck
