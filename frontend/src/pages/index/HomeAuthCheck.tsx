import { useIsAuthenticated } from "@azure/msal-react"
import Home from "./Home"
import Login from "./Login"



const HomeAuthCheck = () => {
  const isAuthenticated = useIsAuthenticated()


  if (isAuthenticated) {
    return <Home/>
  }
  return <Login/>

}


export default HomeAuthCheck