import { useContext } from "react"
import { AppApiContext } from "../providers/AppApiProvider"



const useApiApp = () => {
  const app = useContext(AppApiContext)
  return app
}

export default useApiApp