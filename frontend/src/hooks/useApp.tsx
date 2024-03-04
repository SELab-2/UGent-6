import { useContext } from "react"
import { AppContext } from "../providers/AppProvider"



const useApp = () => {
  const appContext = useContext(AppContext)
  return appContext
}

export default useApp
