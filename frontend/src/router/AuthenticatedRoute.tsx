import { FC, useEffect } from "react"
import useApp from "../hooks/useApp"
import { useAccount, useIsAuthenticated } from "@azure/msal-react"
import { Outlet, useNavigate } from "react-router-dom"
import { AppRoutes } from "../@types/routes"




const AuthenticatedRoute:FC = () => {
  const auth = useIsAuthenticated()
  const s = useAccount()
  const navigate = useNavigate()
  
  useEffect(()=>{
    console.log(s);
      if(!auth) {
        console.log(auth);
        console.log("Not authenticated!");
       // navigate(AppRoutes.HOME)
      }
  },[auth,s])

  if(auth){
    return <Outlet/>
  }

  return null
}

export default AuthenticatedRoute
