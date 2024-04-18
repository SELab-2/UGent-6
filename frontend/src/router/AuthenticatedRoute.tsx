import { FC, useEffect } from "react"
import { useIsAuthenticated, useMsal } from "@azure/msal-react"
import { Outlet, useNavigate } from "react-router-dom"
import { AppRoutes } from "../@types/routes"
import { InteractionStatus } from "@azure/msal-browser"

const AuthenticatedRoute: FC = () => {
  const isAuthenticated = useIsAuthenticated()
  const { inProgress } = useMsal()

  const navigate = useNavigate()

  useEffect(() => {
    if ((inProgress === InteractionStatus.None ||  inProgress === InteractionStatus.Logout ) && !isAuthenticated) {
      // instance.loginRedirect(loginRequest);
      console.log("NOT AUTHENTICATED");
      navigate(AppRoutes.HOME)
    } 
  }, [isAuthenticated,inProgress])


  if (isAuthenticated) {
    return <Outlet />
  }

  return null
}

export default AuthenticatedRoute
