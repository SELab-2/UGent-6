import { FC, useEffect } from "react"
import useApp from "../hooks/useApp"
import { useIsAuthenticated } from "@azure/msal-react"
import { Outlet, useNavigate } from "react-router-dom"
import { AppRoutes } from "../@types/routes"




const AuthenticatedRoute:FC = () => {
  const auth = useIsAuthenticated()
  const navigate = useNavigate()
  
  useEffect(()=>{
      if(!auth) {
        navigate(AppRoutes.HOME)
      }
  },[auth])

  if(auth){
    return <Outlet/>
  }

  return null
}

export default AuthenticatedRoute
