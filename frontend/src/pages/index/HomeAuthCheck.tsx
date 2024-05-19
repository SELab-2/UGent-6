import Home from "./Home"
import LandingPage from "./landing/LandingPage"
import useAuth from "../../hooks/useAuth";

const HomeAuthCheck = () => {
  const auth = useAuth()
  auth.updateAccount()
  if (auth.isAuthenticated) {
    return <Home/>
  }
  return <LandingPage/>
}

export default HomeAuthCheck
