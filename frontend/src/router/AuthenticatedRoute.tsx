import {FC, useEffect} from "react"
import {Outlet, useNavigate} from "react-router-dom"
import {AppRoutes} from "../@types/routes"
import useAuth from "../hooks/useAuth";
import {LoginStatus} from "../@types/appTypes";

const AuthenticatedRoute: FC = () => {
  const auth = useAuth()

  const navigate = useNavigate()

  useEffect(() => {
    if ((auth.loginStatus === LoginStatus.LOGGED_OUT ||  auth.loginStatus === LoginStatus.LOGOUT_IN_PROGRESS ) && !auth.isAuthenticated) {
      // instance.loginRedirect(loginRequest);
      console.log("NOT AUTHENTICATED");
      navigate(AppRoutes.HOME)
    } 
  }, [auth])


  if (auth.isAuthenticated) {
    return <Outlet />
  }

  return null
}

export default AuthenticatedRoute
