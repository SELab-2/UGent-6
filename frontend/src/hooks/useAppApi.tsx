import { useContext } from "react"
import { AppApiContext } from "../providers/AppApiProvider"



const useAppApi = () => {
  const app = useContext(AppApiContext)
  return app
}

export default useAppApi