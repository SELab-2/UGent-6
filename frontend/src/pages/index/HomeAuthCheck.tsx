import { useIsAuthenticated } from "@azure/msal-react"
import Home from "./Home"
import LandingPage from "./landing/LandingPage"



const HomeAuthCheck = () => {
  const isAuthenticated = useIsAuthenticated()


  if (isAuthenticated) {
    return <Home/>
  }
  return <LandingPage/>

}


export default HomeAuthCheck