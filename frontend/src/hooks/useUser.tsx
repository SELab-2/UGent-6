import { useContext } from "react"
import { UserContext } from "../providers/UserProvider"



const useUser = () => {
  const user = useContext(UserContext)
  return user
}

export default useUser