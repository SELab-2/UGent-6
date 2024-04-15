import { FC, PropsWithChildren } from "react"
import useUser from "./useUser"



const AdminView:FC<PropsWithChildren> = ({children}) => {
  const {user} = useUser()
  return user?.role === "admin" ?  <>{children}</> : null
}

export default AdminView
